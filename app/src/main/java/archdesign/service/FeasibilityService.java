package service;

import config.spec.BoxRuleSpecification;
import config.spec.ContainerRuleSpecification;
import entities.Art;
import entities.Box;
import entities.enums.BoxType;
import entities.enums.ContainerType;
import interactor.UserConstraints;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Acts as the "Rule Checker" for the packing process.
 * This service determines all the valid, rule-compliant ways an item can be packed.
 * It does not decide which option is best; that is the role of the OptimizationService.
 */
public class FeasibilityService {

    // The service holds the rule sets it needs to perform its checks.
    private final List<BoxRuleSpecification> boxRules;
    private final List<ContainerRuleSpecification> containerRules; // Added field for container rules

    /**
     * Constructs a FeasibilityService.
     * It is initialized with all the rules it needs to enforce.
     * @param boxRules The list of art-to-box packing rules.
     * @param containerRules The list of box-to-container packing rules.
     */
    public FeasibilityService(List<BoxRuleSpecification> boxRules, List<ContainerRuleSpecification> containerRules) {
        this.boxRules = boxRules;
        this.containerRules = containerRules;
    }

    /**
     * Finds ALL valid packing options for a given Art object based on the rule set.
     * <p>
     * This method iterates through the entire list of rules and collects every valid
     * option that the Art satisfies. The returned list is ordered by the priority
     * defined in the RuleProvider.
     *
     * @param art The Art object to evaluate.
     * @param constraints The user-defined constraints for this packing run.
     * @return A List containing ALL valid PackingOptions, ordered by rule priority.
     * The list may be empty if no options are found.
     */
    public List<PackingOption> getValidPackingOptions(Art art, UserConstraints constraints) {
        List<PackingOption> validOptions = new ArrayList<>();

        for (BoxRuleSpecification rule : boxRules) {
            if (matches(art, rule)) {
                BoxType potentialBoxType = rule.getAllowedBoxType();
                
                // Final check against the user's whitelist for allowed box types.
                List<BoxType> allowedBoxTypes = constraints.getAllowedBoxTypes();
                if (allowedBoxTypes.isEmpty() || allowedBoxTypes.contains(potentialBoxType)) {
                    // The option is valid, add it to our list of possibilities.
                    PackingOption validOption = new PackingOption(potentialBoxType, rule.getCapacity());
                    validOptions.add(validOption);
                }
            }
        }

        // The returned list might contain duplicates if rules are configured that way,
        // though with the current RuleProvider structure, it won't.
        // Returning a distinct list is safer.
        return validOptions.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Finds all valid container options for a given Box object.
     *
     * @param box The Box object to evaluate.
     * @param constraints The user-defined constraints for this packing run.
     * @return A List of all valid ContainerOptions; the list may be empty.
     */
    public List<ContainerOption> getValidContainerOptions(Box box, UserConstraints constraints) {
        List<ContainerOption> potentialOptions = containerRules.stream()
                .filter(rule -> rule.getAllowedBoxType() == box.getBoxType())
                .map(rule -> new ContainerOption(rule.getContainerType(), rule.getCapacity()))
                .collect(Collectors.toList());

        List<ContainerType> allowedContainerTypes = constraints.getAllowedContainerTypes();
        if (!allowedContainerTypes.isEmpty()) {
            return potentialOptions.stream()
                    .filter(option -> allowedContainerTypes.contains(option.containerType()))
                    .collect(Collectors.toList());
        }
        return potentialOptions;
    }

    /**
     * A private helper method to check if an Art object satisfies ONE conditions of a given rule.
     *
     * @param art The Art object to check.
     * @param rule The rule to check against.
     * @return true if the Art satisfies ONE conditions of the rule, false otherwise.
     */
    private boolean matches(Art art, BoxRuleSpecification rule) {
        if (rule.getMaterial() != null && art.getMaterial() != rule.getMaterial()) {
            return false;
        }

        boolean widthMatches = art.getWidth() >= rule.getMinWidth() && art.getWidth() <= rule.getMaxWidth();
        boolean heightMatches = art.getHeight() >= rule.getMinHeight() && art.getHeight() <= rule.getMaxHeight();

        return widthMatches || heightMatches;
    }
}