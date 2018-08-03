# import os
# path = r'C:\Users\Abhishek\eclipse-workspace\Maekawa_Protocol\node_5'
# files = []
# for i in os.listdir(path):
#     if os.path.isfile(os.path.join(path,i)) and 'test_data_20_5' in i.startswith('test_data_'):
#         files.append(i)

# import os

# prefixed = [filename for filename in os.listdir(path) if filename.contains("test_data_")]
# print(prefixed)

import fnmatch
import os
import csv
import itertools

arr1 = [5,20,50]
arr2 = [5,20,50]
comb = list(itertools.product(arr1, arr2))

listOfSum = []
listOfTime = []
path = r'C:\Users\Abhishek\eclipse-workspace\Maekawa_Protocol\node_5'
for i in comb:
    totalMsgComp = 0
    totalTime = 0
    msgComp = []
    throughput = []
    files = []
    for file in os.listdir(path):
        if fnmatch.fnmatch(file, '*_' + str(i[0]) + '_' + str(i[1]) + '.csv'):
            files.append(file)

    for file in files:
        with open(file) as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            row = list(readCSV)
            msgComp.append(row[10])
            throughput.append(row[11])


    for msg in msgComp:
        totalMsgComp += int(msg[0].split(": ", 1)[1])

    for time in throughput:
        totalTime += int(time[0].split(": ", 1)[1])
    listOfSum.append(totalMsgComp)
    listOfTime.append(totalTime)
print(listOfSum)
print(listOfTime)