import numpy as np
import matplotlib.pyplot as plt
import time
import random
from matplotlib import interactive
from Data.StatisticData import Data
from utility import PathUtility
from utility import Global

################  dbpedia
base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\res\\O3-dbpedia\\'
base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\res\\IO-dbpedia\\'
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\res\\IO-yago\\'

fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300, t=1, ns=200, r=3, k=5, nw=3, wf=10000, dr=3, opt='O5')
indexs = Data.get_equal_k_simple_indexs(fp=fp, k=5)

print(indexs)

data = Data.getData_by_indexs(indexs=indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300,
                                                 t=1, ns=200, r=3, k=5, nw=3, wf=10000, dr=3, opt="O0"),
                                                 time_total_threshold=12000000)
print(data)

# data = Data.getData_by_indexs(indexs=indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300,
#                                                  t=1, ns=200, r=3, k=5, nw=3, wf=10000, dr=3, opt="O5"),
#                                                  time_total_threshold=1200000)
# print(data)



#################  yago
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\res\\O3-yago\\'
#
# fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300, t=1, ns=200, r=3, k=5, nw=3, wf=1000, dr=3, opt='O5')
# indexs = Data.get_equal_k_simple_indexs(fp=fp, k=5)
#
# # print(indexs)
#
# data = Data.getData_by_indexs(indexs=indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300,
#                                                  t=1, ns=200, r=3, k=5, nw=3, wf=1000, dr=3, opt="O0"),
#                                                  time_total_threshold=1200000)
# print(data)
#
# data = Data.getData_by_indexs(indexs=indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=300,
#                                                  t=1, ns=200, r=3, k=5, nw=3, wf=1000, dr=3, opt="O5"),
#                                                  time_total_threshold=1200000)
# print(data)





