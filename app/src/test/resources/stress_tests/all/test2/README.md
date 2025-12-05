# Stress tests
Define your input.csv and expected_output.json files in this repository. If you define multiple test cases, place each one into a separate sub-directory.

Use the provided `expected_output.json` template to fill in the details of the expected output that corresponds to your input.csv.

---

### Explanation/Justification

(Note: this is assuming the client does NOT accept crates)

200 paper print framed (glass) 33 x 43
200 * ceil(43 * 33 * 0.0098) = 2,800lbs

200 paper print framed (glass) 32 x 56
200 * ceil(56 * 32 * 0.0098) = 3,600lbs

6 pieces per standard box 
400 / 6 = 66.67 -> 67 standard boxes needed

oversize pallets fit 5 standard boxes, so you'd need 67 / 5 = 13.4 -> 14 oversize pallets which is a total of 14 * 75 = 1,050lbs

if we used standard pallets instead we'd need 67 / 4 = 16.75 -> 17 standard pallets which is a total of 17 * 60 = 1,020lbs

if we mix standard and large pallets we would need 11 large pallets (825 lbs) (packing 55 boxes) and 3 standard pallets (180 lbs) (packing the remaining 12 boxes), with total pallet weight 1005 lbs
So our total art weight is 6,400lbs and our total packing weight is 1,005 lbs, so our total shipment weight is 7,405lbs
