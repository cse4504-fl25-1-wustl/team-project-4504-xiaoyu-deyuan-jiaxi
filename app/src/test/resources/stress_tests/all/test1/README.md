# Non-Sunrise packing test cases
This test is checking the packing logic of choosing between same tare weight standard vs oversized pallets.

The test case in this directory assume that the client is not "Sunrise" and that they don't accept crates, which means that they only accepts pallets (no crates), and that we can fit the usual 6 Paper Prints into a Standard Box

These estimates are based on the material depth:
  - paper prints - 1.8334"
  - canvases - 2.75"

Assumed Packing Strategy: Pack by Depth

With 480 Standard sized arts, we can fit them into 80 Standard Boxes. Those 80 Standard Boxes can fit onto either 20 Standard Pallets, or 16 Oversize Pallets, both of which have a tare weight of 1200 lbs. Since they have the same tare weight we put them into 16 Oversize Pallets as that is numerically less than 20. 
