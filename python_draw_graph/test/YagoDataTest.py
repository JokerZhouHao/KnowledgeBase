from Data.StatisticData import Data
from utility import PathUtility

######################  词频测试  ######################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new1\\word_frequency\\'
# # testSampleResultFile.SPBest.nwlen=50000000.mds=50000000.t=0.ns=200.r=2.k=1.nw=5.wf=100.dr=7
# type = 0
# wfs = (0, 50, 100, 250, 500, 1000, 10000, 100000, 1000000)
# for wf in wfs:
#     data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=3, k=5, nw=5, wf=wf, dr=7), time_total_threshold=120000)
#     print(data)


###################### k测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new1\\nw_k_dr\\'
# type = 1
# ks = (1, 3, 5, 8, 10, 15, 20)
# for k in ks:
#     data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBase', nwlen=1000000, mds=50000000, t=type, ns=200, r=2, k=k, nw=5, wf=50, dr=7), time_total_threshold=12000000)
#     print(data)

###################### radius_len测试  ###################
base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new1\\radius_len\\'
type = 0
# k = (1, 3, 5, 8, 10, 15, 20)
radius = (1, 2, 3)
lens = (100000, 1000000, 10000000)
for radiu in radius:
    for len in lens:
        data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=len, mds=50000000, t=type, ns=200, r=radiu, k=5, nw=5, wf=50, dr=7), time_total_threshold=120000)
        print(data)
    print('\n')

###################### diff_size测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new1\\diff_size\\'
#
# algs = ('SPBase', 'SPBest')
# types = (0, 1)
# sizes = ('2000000\\', '4000000\\', '6000000\\', 'org\\')
#
# for alg in algs:
#     for type in types:
#         print('---------------------------------  ' + alg + '  ' + str(type) + '   -------------------------------')
#         for size in sizes:
#             data = Data.getData(fp=PathUtility.sample_res_path(base_dir + size, sp=alg, nwlen=1000000, mds=50000000, t=type, ns=200, r=2, k=5, nw=5, wf=50, dr=7), time_total_threshold=12000000)
#             print(data)
#         print('----------------------------------------------------------------\n\n\n')


