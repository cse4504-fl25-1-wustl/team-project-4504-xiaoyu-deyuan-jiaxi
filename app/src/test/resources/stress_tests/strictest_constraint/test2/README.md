# Stress tests

1,200,1,Canvas - Gallery,30.0,30.0,N/A,N/A,N/A
2,100,2,Canvas - Float Frame,40.0,40.0,N/A,N/A,N/A
3,100,3,Paper Print - Framed,40.1,50.0,Glass,N/A,N/A
4,100,4,Acoustic panel,60.0,40.0,N/A,N/A,N/A

Largest items are packed in boxes first, so line number 4 will be packed first, then 3, then 2, then 1.

60 by 40 means that those pieces are classified as "oversized" rather than "standard sized." However, they fit in large boxes because 36.5 < 40 < 43.5, and the largest dimension 60 < 88.
4 acoustic panels fit in each box, so 100 / 4 = 25 large boxes

Next, the prints (line number 3) are packed. 40.1 by 50 means that those pieces are classified as "oversized" rather than "standard sized." However, they fit in large boxes because 36.5 < 40.1 < 43.5, and the largest dimension 50 < 88. 6 prints fit in a box. 100 / 6 = 16 R4, so 17 large boxes are required, with 1 large box with only 4 prints. Because there are no more prints to pack, no other pieces can be added to that box because if there's a non-print in the box, then the capacity is 4, not 6 (we use the most restrictive capacity for packing mixed mediums).

Next, line number 2 is packed. Both dimensions are > 36.5 and < 43.5, so they fit in large boxes and are classified as "standard sized." 4 canvases fit in each box, so 100 / 4 = 25 large boxes with no space left over. 

Then, line number 1 is packed. Dimensions are 30 by 30 (both less than 36.5) so they are classified as "standard sized" and fit in standard boxes. 4 can fit in each box, so 200 / 4 = 50 standard boxes. 

Total standard sized pieces: 100 + 200 = 300 (the other 200 pieces are considered "oversized" as explained above).

Total number of standard boxes: 50
Total number of large boxes: 25 + 17 + 25 = 67
Total number of boxes: 50 + 67 = 117

An overszed pallet can fit 3 large boxes
Standard pallets fit 4 standard boxes and 3 large boxes. Oversized pallets fit 5 standard boxes. 

using oversized pallets: 
50 standard boxes / 5 standard boxes per oversized pallet = 10 oversized pallets
67 large boxes / 3 large boxes per oversized pallet = 22 oversized pallets with 1 large box left over. That box can fit on a standard pallet. 

total pallets = 10 oversized pallets + 22 oversized pallets + 1 standard pallet = 32 oversized + 1 standard pallet

Calculating packaging weight:
oversized pallets: 32 * 75 = 2400 lbs
standard pallet: 1 * 60 = 60 lbs
total = 2400 + 60 = 2460 lbs

using standard pallets:
50 standard boxes / 4 standard boxes per standard pallet = 12 standard pallets, 2 standard boxes remaining
67 large boxes / 3 large boxes per standard pallet = 22 standard pallets, 1 large box remaining
The leftover 2 standard boxes and 1 large box can fit on 1 standard pallet

Total number of standard pallets = 12 + 22 + 1 = 35 

Calculating packaging weight:
standard pallet: 35 * 60 = 2100 lbs

2100 lbs < 2460 lbs, so the boxes will all be packed on standard pallets

Calculating artwork weight (weight of each individual item is rounded up): 
1: 200 * ceil(30 * 30 * 0.0061) = 200 * 6 = 1200 lbs
2: 100 * ceil(40 * 40 * 0.0085) = 100 * 14 = 1400 lbs
3: 100 * ceil(40.1 * 50 * 0.0098) = 100 * 20 = 2000 lbs
4: 100 * ceil(60 * 40 * 0.0038) = 100 * 10 = 1000 lbs

Total artwork weight: 1200 + 1400 + 2000 + 1000 = 5600 lbs


Total packing weight = 2100 lbs

Total shipping weight = 5600 + 2100 = 7700 lbs