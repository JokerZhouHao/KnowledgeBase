import numpy as np
import scipy.io
import random

def creatdatasets(data):
    change_data=data[:]
    n = change_data.shape[0]
    nchange = random.randint(3, 6)
    timestep = random.randint(n/2, n)
    datasets = data[:]
    for st in range(timestep):
        for i in range(nchange):
            i = random.randint(0, n-1)
            j = random.randint(0, n-1)
            if i != j:
                if (change_data[i][j]-1).any():
                    change_data[i][j] =1
                    change_data[j][i] =1
                else:
                    change_data[i][j] =0
                    change_data[j][i] =0
        datasets=np.append(datasets, change_data)
    datasets = datasets.reshape(timestep+1,2, 2)
    return datasets

data = np.array([[1, 1], [1, 1]])
change_data = data[1:]
change_data[0] = 2
# change_data[0][0] = 2
print(data)
print(change_data)


