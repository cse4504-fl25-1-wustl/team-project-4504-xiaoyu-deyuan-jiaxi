#!/bin/bash
process_csv_files() {
    local input_dir="$1"
    local client_config="$2"    
    while IFS= read -r -d '' csv_file; do
        # print out the file name
        echo "$csv_file"
        
        # 1. run your application with this CSV file as input
        # 2. get the output as json file
        # 3. If not output is produced, it's a failure, otherwise
        # 4. run the json_response_comparator script, comparing your output
        #    with expected_output.json
	# 5. If json_response_comparator script terminates successfully (exit code 0),
        #    then the test passes. Otherwise, it fails.
    done < <(find "$input_dir" -type f -name "*.csv" -print0)
}

INPUT_DIR="/path/to/test_case"

# pass client configuration that does not allow crates
process_csv_files "$INPUT_DIR"/box_packing
process_csv_files "$INPUT_DIR"/pallet_packing
# pass client configuration that allows crates
process_csv_files "$INPUT_DIR"/crate_packing
