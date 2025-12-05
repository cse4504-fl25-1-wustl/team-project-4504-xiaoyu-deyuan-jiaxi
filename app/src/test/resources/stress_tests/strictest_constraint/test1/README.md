# Stress tests
Define your input.csv and expected_output.json files in this repository. If you define multiple test cases, place each one into a separate sub-directory.

Use the provided `expected_output.json` template to fill in the details of the expected output that corresponds to your input.csv.

# Stress Test: Mixed Medium Capacity Violation (based on most restrictive rule)

For any box containing mixed mediums (such as Prints and Canvas/Acoustic Panels), the capacity must drop to the minimum allowed value of 4 items (BoxCapacity.MIN_CAPACITY).

Exploits a bug where the system checks for space before checking for medium-type capacity rules. (ours had this flaw)

Core Test Goal: Allocation and Efficiency

This test is designed to verify that the packing algorithm correctly calculates the minimum number of boxes by optimally segregating items by medium to utilize the full capacity of each box type, especially when dealing with remainders.

Input Design: The input contains a non-divisible quantity of items, all sized 38" x 40" (to ensure Large Box classification).

- Acoustic Panels (Capacity 4): 81 piecesc
- Prints (Capacity 6): 321 pieces
Total Pieces: 402

Expected Optimal Packing Logic (The Correct Output): The system must pack the maximum number of items into the minimum number of boxes by grouping same-medium items together:
- Acoustic Panels (Capacity 4):$81 \div 4 = 20$ full boxes with $1$ item remaining.
- Total Panel Boxes: 21Prints (Capacity 6):$321 \div 6 = 53$ full boxes with $3$ items remaining.
- Total Print Boxes: 54
- Total Large Boxes: $21 + 54 = \mathbf{75 \text{ Boxes}}$

** NOTE ** Our own code allows the 5th item to be added in all cases, resulting in an overfilled box (5 items in a capacity 4 box).Actual Boxes: 80 groups $\times$ 1 box/group = 81 Boxes

Total Pieces: 81 + 321 = 402
Total Boxes: (81/4 $\rightarrow$ 21 Boxes) + (321/6 $\rightarrow$ 54 Boxes)75 Boxes
Total Artwork Weight: (321 P $\times$ 16 lb) + (81 A $\times$ 6 lb) = 5136 + 4865622.0
Total Pallets: 74 Boxes / 3 boxes per pallet = 25 Pallets
Total Packaging Weight: $25 \times 60 \text{ lb/pallet}$1500.0
Final Shipment Weight: $5622.0 + 1500.0 = 7122.0
