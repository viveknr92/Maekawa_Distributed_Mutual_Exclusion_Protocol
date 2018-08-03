import fnmatch
import os
import csv
import itertools

arr1 = [5,20,50]
arr2 = [5,20,50]
comb = list(itertools.product(arr1, arr2))

listOfSum = []
listOfTime = []
listOfAvgResTime = []
path = r'C:\Users\Abhishek\eclipse-workspace\Maekawa_Protocol\node_1'
for i in comb:
    totalMsgComp = 0
    totAvgRespTime = 0
    totalTime = 0
    totResTime = 0
    msgComp = []
    throughput = []
    avgResTime = []
    files = []
    for file in os.listdir(path):
        if fnmatch.fnmatch(file, '*_' + str(i[0]) + '_' + str(i[1]) + '.csv'):
            files.append(file)

    for file in files:
        with open(file) as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            row = list(readCSV)
            for i in row[:-2]:
                totResTime += int(i[0].split(": ", 1)[1])
            totResTime /= 9
            msgComp.append(row[10])
            throughput.append(row[11])
            avgResTime.append(totResTime)

    for msg in msgComp:
        totalMsgComp += int(msg[0].split(": ", 1)[1])

    for time in throughput:
        totalTime += int(time[0].split(": ", 1)[1])

    for restime in avgResTime:
        totAvgRespTime += restime
    totAvgRespTime /= 5

    listOfSum.append(totalMsgComp)
    listOfTime.append(totalTime)
    listOfAvgResTime.append(totAvgRespTime)

print("Message Complexity: ")
for i in listOfSum:
    print(i)

print("Throughput for 10secs: ")
for i in listOfTime:
    print((50/(i*10**-3))*10)

print()
print("Total Avg Resp Time: ")
for i in listOfAvgResTime:
    print(i)