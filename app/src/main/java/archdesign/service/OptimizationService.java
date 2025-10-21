package archdesign.service;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.service.costing.ShippingCostStrategy;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 优化服务 - 直接优化Container类型的选择
 * 目标：最小化总重量（container重量 + arts重量）
 */
public class OptimizationService {

    private final FeasibilityService feasibilityService;
    private final ShippingCostStrategy costStrategy;
    private int boxIdCounter = 1;
    private int containerIdCounter = 1;

    public OptimizationService(FeasibilityService feasibilityService, ShippingCostStrategy costStrategy) {
        this.feasibilityService = feasibilityService;
        this.costStrategy = costStrategy;
    }

    public PackingPlan createOptimalPlan(List<Art> artsToPack, UserConstraints constraints) {
        if (artsToPack == null || artsToPack.isEmpty()) {
            return new PackingPlan(new ArrayList<>(), 0.0);
        }

        try {
            Loader.loadNativeLibraries();
            
            MPSolver solver = MPSolver.createSolver("SCIP");
            if (solver == null) {
                System.err.println("无法创建求解器，回退到启发式算法");
                return fallbackHeuristic(artsToPack, constraints);
            }

            solver.setTimeLimit(60000); // 60秒超时

            // 第一步：分析每个art需要的box空间
            List<ArtBoxRequirement> artRequirements = new ArrayList<>();
            for (Art art : artsToPack) {
                List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
                if (options.isEmpty()) {
                    System.err.println("Art " + art.getId() + "not packable");
                    continue;
                }
                artRequirements.add(new ArtBoxRequirement(art, options.get(0)));
            }

            // 第二步：按BoxType分组art，计算需要的box数量
            Map<BoxType, Integer> boxesNeeded = calculateBoxesNeeded(artRequirements);
            // 第三步：获取所有可用的container类型及其容量
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities = 
                getContainerCapacities(boxesNeeded.keySet(), constraints);

            // 第四步：为每种container类型创建决策变量
            Map<ContainerType, MPVariable> containerVars = new HashMap<>();
            for (ContainerType containerType : containerCapacities.keySet()) {
                MPVariable var = solver.makeIntVar(0, 100, "container_" + containerType.name());
                containerVars.put(containerType, var);
            }

            // 第五步：添加约束 - 每种box类型必须有足够的container空间
            for (Map.Entry<BoxType, Integer> entry : boxesNeeded.entrySet()) {
                BoxType boxType = entry.getKey();
                int needed = entry.getValue();
                
                MPConstraint constraint = solver.makeConstraint(needed, Double.POSITIVE_INFINITY, 
                    "box_capacity_" + boxType.name());
                
                // 对于每种container，如果它能装这种box，就添加到约束中
                for (Map.Entry<ContainerType, Map<BoxType, Integer>> containerEntry : containerCapacities.entrySet()) {
                    ContainerType containerType = containerEntry.getKey();
                    Map<BoxType, Integer> capacities = containerEntry.getValue();
                    
                    Integer capacity = capacities.get(boxType);
                    if (capacity != null && capacity > 0) {
                        // 这种container能装这种box，每个container提供capacity个位置
                        constraint.setCoefficient(containerVars.get(containerType), capacity);
                    }
                }
            }

            // 第六步：设置目标函数 - 最小化总重量
            MPObjective objective = solver.objective();
            double artsWeight = artsToPack.stream().mapToDouble(Art::getWeight).sum();
            
            for (Map.Entry<ContainerType, MPVariable> entry : containerVars.entrySet()) {
                ContainerType containerType = entry.getKey();
                MPVariable var = entry.getValue();
                
                // 每个container的成本 = container自重
                // (arts重量是固定的，不影响最优化方向)
                objective.setCoefficient(var, containerType.getWeight());
            }
            objective.setMinimization();

            System.out.println("\n开始求解...");

            // 第七步：求解
            final MPSolver.ResultStatus resultStatus = solver.solve();

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {


                // 提取解决方案
                Map<ContainerType, Integer> solution = new HashMap<>();
                double totalContainerWeight = 0;
                int totalContainers = 0;

                for (Map.Entry<ContainerType, MPVariable> entry : containerVars.entrySet()) {
                    ContainerType type = entry.getKey();
                    int count = (int) Math.round(entry.getValue().solutionValue());
                    if (count > 0)
                    {
                        solution.put(type, count);
                        totalContainerWeight += type.getWeight() * count;
                        totalContainers += count;
                    }
                }

                List<Container> containers = buildContainersFromSolution(
                    solution, artRequirements, boxesNeeded, containerCapacities, constraints);

                double totalCost = containers.stream()
                    .mapToDouble(costStrategy::calculateCost)
                    .sum();

                System.out.println("总成本: $" + String.format("%.2f", totalCost));

                return new PackingPlan(containers, totalCost);
                
            } else {
                System.err.println("OR-Tools 未找到可行解: " + resultStatus);
                return fallbackHeuristic(artsToPack, constraints);
            }

        } catch (Exception e) {
            System.err.println("OR-Tools 求解出错: " + e.getMessage());
            e.printStackTrace();
            return fallbackHeuristic(artsToPack, constraints);
        }
    }

    /**
     * 计算需要多少个box
     */
    private Map<BoxType, Integer> calculateBoxesNeeded(List<ArtBoxRequirement> requirements) {
        // 按BoxType和capacity分组
        Map<BoxType, Map<Integer, List<Art>>> groupedByTypeAndCapacity = new HashMap<>();
        
        for (ArtBoxRequirement req : requirements) {
            groupedByTypeAndCapacity
                .computeIfAbsent(req.option.boxType(), k -> new HashMap<>())
                .computeIfAbsent(req.option.capacity(), k -> new ArrayList<>())
                .add(req.art);
        }

        // 计算每种BoxType需要的box数量
        Map<BoxType, Integer> boxesNeeded = new HashMap<>();
        
        for (Map.Entry<BoxType, Map<Integer, List<Art>>> typeEntry : groupedByTypeAndCapacity.entrySet()) {
            BoxType boxType = typeEntry.getKey();
            int totalBoxes = 0;
            
            for (Map.Entry<Integer, List<Art>> capacityEntry : typeEntry.getValue().entrySet()) {
                int capacity = capacityEntry.getKey();
                int artCount = capacityEntry.getValue().size();
                int boxes = (int) Math.ceil((double) artCount / capacity);
                totalBoxes += boxes;
            }
            
            boxesNeeded.put(boxType, totalBoxes);
        }
        
        return boxesNeeded;
    }

    /**
     * 获取每种container对每种box的容量
     */
    private Map<ContainerType, Map<BoxType, Integer>> getContainerCapacities(
            Set<BoxType> boxTypes, UserConstraints constraints) {
        
        Map<ContainerType, Map<BoxType, Integer>> result = new HashMap<>();
        
        for (BoxType boxType : boxTypes) {
            Box testBox = new Box("temp", boxType, 1, 1, 1);
            List<ContainerOption> options = feasibilityService.getValidContainerOptions(testBox, constraints);
            
            for (ContainerOption option : options) {
                result.computeIfAbsent(option.containerType(), k -> new HashMap<>())
                      .put(boxType, option.capacity());
            }
        }
        
        return result;
    }

    /**
     * 根据OR-Tools的解构建实际的containers
     */
    private List<Container> buildContainersFromSolution(
            Map<ContainerType, Integer> solution,
            List<ArtBoxRequirement> artRequirements,
            Map<BoxType, Integer> boxesNeeded,
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities,
            UserConstraints constraints) {

        List<Container> containers = new ArrayList<>();

        // 按BoxType分组arts
        Map<BoxType, List<ArtBoxRequirement>> artsByBoxType = artRequirements.stream()
            .collect(Collectors.groupingBy(req -> req.option.boxType()));

        // 为每种需要的container类型创建实例
        for (Map.Entry<ContainerType, Integer> entry : solution.entrySet()) {
            ContainerType containerType = entry.getKey();
            int count = entry.getValue();
            Map<BoxType, Integer> capacities = containerCapacities.get(containerType);

            // 创建count个这种类型的container
            for (int i = 0; i < count; i++) {
                Container container = createNewContainer(containerType);
                containers.add(container);
            }
        }

        // 现在将boxes分配到containers中
        // 使用First Fit策略
        for (Map.Entry<BoxType, List<ArtBoxRequirement>> entry : artsByBoxType.entrySet()) {
            BoxType boxType = entry.getKey();
            List<ArtBoxRequirement> arts = entry.getValue();

            // 获取capacity
            int capacity = arts.isEmpty() ? 1 : arts.get(0).option.capacity();

            // 创建boxes
            List<Box> boxes = new ArrayList<>();
            for (int i = 0; i < arts.size(); i += capacity) {
                Box box = createNewBox(boxType);
                int end = Math.min(i + capacity, arts.size());
                for (int j = i; j < end; j++) {
                    box.addArt(arts.get(j).art);
                }
                boxes.add(box);
            }

            // 将boxes分配到合适的containers
            for (Box box : boxes) {
                boolean placed = false;
                
                for (Container container : containers) {
                    Map<BoxType, Integer> capacities = containerCapacities.get(container.getContainerType());
                    Integer maxCapacity = capacities.get(boxType);
                    
                    if (maxCapacity != null && maxCapacity > 0) {
                        // 检查这个container还能装下这个box吗
                        long currentBoxCount = container.getBoxesInContainer().stream()
                            .filter(b -> b.getBoxType() == boxType)
                            .count();
                        
                        if (currentBoxCount < maxCapacity) {
                            container.addBox(box);
                            placed = true;
                            break;
                        }
                    }
                }
                
                if (!placed) {
                    System.err.println("cannot place box " + box.getId());
                }
            }
        }

        return containers;
    }

    // Fallback方法保持不变
    private PackingPlan fallbackHeuristic(List<Art> artsToPack, UserConstraints constraints) {
        List<Container> containers = new ArrayList<>();
        List<Art> sortedArts = new ArrayList<>(artsToPack);
        sortedArts.sort(Comparator.comparingDouble(Art::getWeight).reversed());
        
        for (Art art : sortedArts) {
            boolean placed = false;
            
            for (Container container : containers) {
                if (tryAddArtToContainer(container, art, constraints)) {
                    placed = true;
                    break;
                }
            }
            
            if (!placed) {
                Container newContainer = createNewContainerForArt(art, constraints);
                if (newContainer != null) {
                    containers.add(newContainer);
                }
            }
        }
        
        double totalCost = containers.stream()
            .mapToDouble(costStrategy::calculateCost)
            .sum();
        
        return new PackingPlan(containers, totalCost);
    }

    private boolean tryAddArtToContainer(Container container, Art art, UserConstraints constraints) {
        List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
        if (options.isEmpty()) return false;
        
        Box targetBox = null;
        for (Box box : container.getBoxesInContainer()) {
            if (box.getBoxType() == options.get(0).boxType() && 
                box.getArtsInBox().size() < options.get(0).capacity()) {
                targetBox = box;
                break;
            }
        }
        
        if (targetBox != null) {
            targetBox.addArt(art);
            return true;
        }
        
        List<ContainerOption> containerOptions = feasibilityService.getValidContainerOptions(
            createNewBox(options.get(0).boxType()), constraints);
        
        for (ContainerOption option : containerOptions) {
            if (option.containerType() == container.getContainerType() &&
                container.getBoxesInContainer().size() < option.capacity()) {
                Box newBox = createNewBox(options.get(0).boxType());
                newBox.addArt(art);
                container.addBox(newBox);
                return true;
            }
        }
        
        return false;
    }

    private Container createNewContainerForArt(Art art, UserConstraints constraints) {
        List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
        if (options.isEmpty()) return null;
        
        Box box = createNewBox(options.get(0).boxType());
        box.addArt(art);
        
        List<ContainerOption> containerOptions = feasibilityService.getValidContainerOptions(box, constraints);
        if (containerOptions.isEmpty()) return null;
        
        Container container = createNewContainer(containerOptions.get(0).containerType());
        container.addBox(box);
        return container;
    }

    private Box createNewBox(BoxType type) {
        String id = "Box-" + boxIdCounter++;
        return new Box(id, type, type.getWidth(), type.getLength(), type.getMinHeight());
    }

    private Container createNewContainer(ContainerType type) {
        String id = "Container-" + containerIdCounter++;
        return new Container(id, type, type.getWidth(), type.getLength(), 
            type.getMinHeight(), type.getWeight(), type.getBaseHeight());
    }

    /**
     * Art和其对应的box需求
     */
    private static class ArtBoxRequirement {
        final Art art;
        final PackingOption option;
        
        ArtBoxRequirement(Art art, PackingOption option) {
            this.art = art;
            this.option = option;
        }
    }
}