import os
input_file = "std_in.txt"
output_file = "trans_to_info.txt"

with open(input_file,"r",encoding = "utf-8") as f:
    lines = f.readlines()

lines = lines[1:]

with open(output_file,"w",encoding="utf-8") as out:
    for line in lines:
        parts = line.strip().split()
        transformed = ",".join(f''' "{l}" '''for l in parts)
        out.write(f"inputInfo.add(new ArrayList<>(Arrays.asList({transformed})));\n")

