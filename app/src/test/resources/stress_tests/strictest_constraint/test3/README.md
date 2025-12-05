# Stress tests
Define your input.csv and expected_output.json files in this repository. If you define multiple test cases, place each one into a separate sub-directory.

Use the provided `expected_output.json` template to fill in the details of the expected output that corresponds to your input.csv.

Define your input.csv and expected_output.json files in this repository. If you define multiple test cases, place each one into a separate sub-directory.

Use the provided expected_output.json template to fill in the details of the expected output that corresponds to your input.csv.

NOTE: most restrictive medium rule for mixed mediums

input.csv
line number,quantity,tag number,Final medium,Outside Size Width,Outside Size Height,Glazing,Frame 1 Moulding,Hardware<br>
1,50,1,Paper Print - Framed,30,40,Regular Glass,N/A,N/A<br>
2,45,2,Paper Print - Framed,38,42,Regular Glass,N/A,N/A<br>
3,30,3,Canvas - Float Frame,32,40,N/A,N/A,N/A<br>
4,80,4,Acoustic panel,30,38,N/A,N/A,N/A<br>
5,100,5,Paper Print - Framed,40,45,Regular Glass,N/A,N/A<br>
6,80,6,Canvas - Gallery,32,36.5,N/A,N/A,N/A<br>
7,16,7,Acoustic panel - framed,30,36,N/A,N/A,N/A<br>

How the boxes and pallets were filled:<br>
After some edits, I believe we arrived at the proper output. The boxes are packed correclty following all constraints. Large boxes are used for line items 5 and 2 as one fo the artpiece dimensions exceeds 36.5". Six items can be packed in each box because the medium for line items 5 and 2 is Paper Print - Framed which holds 6 per box. For box 25, 1 larger art piece (line item 2 piece) started the box so additional 1 pieces filled the box even though they are standard pieces. After box 25 is filled, 7 standard boxes were used to fill line item 1 as dimensions were below 36.5" and Paper Print - Framed can fit 6 items in a standard box. Following that line items 3,6,4, and 7 all had constraints of 4 per box, so the remaining boxes were all standard sicne dimensions of those line items allowed and had 4 art pieces some with mixed art items and some with the same. The last box holds one leftover item. 

The 25 large boxes go on 9 standard palelts, 8 of which are full and 1 has space for 2 standard boxes. This means we have 58 standard boxes left over. We can pack those on 10 oversized pallets (for a total of 50 boxes) and 2 standard pallets (for the remaining 8 boxes). This gives us a total of 11 standrad pallets and 10 oversized pallets: 11 * 60 + 10 * 75 = 1410

