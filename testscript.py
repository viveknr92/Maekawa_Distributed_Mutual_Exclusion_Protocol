import fnmatch
import os
import csv
import itertools

arr1 = [5,20,50]
arr2 = [5,20,50]
comb = list(itertools.product(arr1, arr2))
msgComp = []
path = r'C:\Users\Abhishek\eclipse-workspace\Maekawa_Protocol'
for file in os.listdir(path):
    if fnmatch.fnmatch(file, '*.csv'):
        print(file)
        with open(file, encoding="utf8") as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            row = list(readCSV)
            for i in row[1:]:
                msgComp.append(i[3])

for i in msgComp:
    print(i)