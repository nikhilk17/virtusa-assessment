import os
from datetime import datetime
import sys  # was gonna add cli args but didnt get to it

# the keywords we look for in logs
keywords = ["CRITICAL", "ERROR", "FAILED LOGIN"]

# count how many of each
counts = {
    "CRITICAL": 0,
    "ERROR": 0,
    "FAILED LOGIN": 0
}

input_file = "servers.log"

# output file - appends todays date
dt = datetime.now().strftime("%Y-%m-%d")
output_file = "security_alert_" + dt + ".txt"


# reads the log file and picks out the bad lines
def scan_logs():
    try:
        f = open(input_file, "r")
        out = open(output_file, "w")
        
        for line in f:
            upper_line = line.upper()
            
            # check each keyword one by one
            for kw in keywords:
                if kw in upper_line:
                    counts[kw] = counts[kw] + 1
                    out.write(line)
                    break  # dont count same line twice
        
        f.close()
        out.close()
        # print("debug: done scanning file")
        
        print("\nLog processing completed!")
    
    except FileNotFoundError:
        print("server.log file not found!")
    except Exception as e:
        print("something went wrong: " + str(e))


def show_summary():
    # print the summery of what we found
    print("\n------ Security Alert Summary ------")
    for k in counts:
        val = counts[k]
        print(k + ": " + str(val))
    print("--------------------------------------")


# check if output file got created and how big
def check_file_size():
    if os.path.exists(output_file) == True:
        sz = os.path.getsize(output_file)
        print("\nAlert file created: " + output_file)
        print("File size: " + str(sz) + " bytes")
    else:
        print("Alert file was not created!")


# MAIN
scan_logs()
show_summary()
check_file_size()
