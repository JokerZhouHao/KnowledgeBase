from Data.StatisticData import Data
from utility import PathUtility

######################  indexs测试  ######################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\word_frequency\\'
#
# type = 0
#
# indexs = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=3, k=5, nw=5, wf=100, dr=7), time_total_threshold=120000)
# wfs = (100, 250, 500, 1000, 10000, 100000, 1000000)
# for wf in wfs:
#     data = Data.getData_by_indexs(indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=3, k=5, nw=5, wf=wf, dr=7), time_total_threshold=120000)
#     print(data)

######################  词频测试  ######################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\radius_len\\'
# type = 0
# wfs = (100, 250, 500, 1000, 10000, 100000, 1000000)
# for wf in wfs:
#     data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=3, k=5, nw=5, wf=wf, dr=7), time_total_threshold=120000)
#     print(data)

###################### top-k测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\k_n_wrod_dr\\'
# algs = ('SPBase', 'SPBest')
# types = (0, 1)
# ks = (1, 3, 5, 8, 10, 15, 20)
#
# index0 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=0, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# index1 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=1, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# indexs = (index0, index1)
#
# for alg in algs:
#     for type in types:
#         print('---------------------------------  ' + alg + '  ' + str(type) + '   -------------------------------')
#         for k in ks:
#             # data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg, nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=k, nw=5, wf=1000, dr=7), time_total_threshold=120000)
#             data = Data.getData_by_indexs(indexs[type], fp=PathUtility.sample_res_path(base_dir, sp=alg, nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=k, nw=5, wf=1000, dr=7), time_total_threshold=120000)
#             print(data)
#
#     print('----------------------------------------------------------------\n\n\n')

###################### n_word测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\k_n_wrod_dr\\'
# algs = ('SPBase', 'SPBest')
# types = (0, 1)
# nws = (1, 3, 5, 8, 10)
#
# index0 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=0, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# index1 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=1, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# indexs = (index0, index1)
#
# for alg in algs:
#     for type in types:
#         print('---------------------------------  ' + alg + '  ' + str(type) + '   -------------------------------')
#         for nw in nws:
#             # data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg, nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=5, nw=nw, wf=1000, dr=7), time_total_threshold=120000)
#             data = Data.getData_by_indexs(indexs[type], fp=PathUtility.sample_res_path(base_dir, sp=alg, nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=5, nw=nw, wf=1000, dr=7), time_total_threshold=120000)
#             print(data)
#
#     print('----------------------------------------------------------------\n\n\n')

###################### dr测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\k_n_wrod_dr\\'
# algs = ('SPBase', 'SPBest')
# types = (1, )
# drs = (0,3,7,15,30,50,100,150)
#
# index0 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBase', nwlen=10000000, mds=50000000, t=1, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# index1 = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=1, ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
# indexs = (index0, index1)
#
# for alg_index in range(len(algs)):
#     for type in types:
#         print('---------------------------------  ' + algs[alg_index] + '  ' + str(type) + '   -------------------------------')
#         for dr in drs:
#             data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=algs[alg_index], nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=5, nw=5, wf=1000, dr=dr), time_total_threshold=120000)
#             # data = Data.getData_by_indexs(indexs[alg_index], fp=PathUtility.sample_res_path(base_dir, sp=algs[alg_index], nwlen=10000000, mds=50000000, t=type, ns=200, r=1, k=5, nw=5, wf=1000, dr=dr), time_total_threshold=120000)
#             print(data)
#
#     print('----------------------------------------------------------------\n\n\n')


###################### radius_k测试  ###################
# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new\\radius_k\\'
# type = 1
# # k = (1, 3, 5, 8, 10, 15, 20)
# radius = (1, 2, 3, 5)
# for radiu in radius:
#     data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=radiu, k=20, nw=5, wf=50, dr=7), time_total_threshold=12000000)
#     print(data)

###################### radius_len测试  ###################
base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\DBpedia_single_date\\new1\\radius_len\\'

types = (0, )

indexs = Data.get_in_time_simple_indexs(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=100000, mds=50000000, t=types[0], ns=200, r=1, k=5, nw=5, wf=1000, dr=7), time_total_threshold=1200000)

radius = (1, 2, 3)
lens = (100000, 1000000, 10000000)
for type in types:
    for radiu in radius:
        for len in lens:
            data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=len, mds=50000000, t=type, ns=200, r=radiu, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
            # data = Data.getData_by_indexs(indexs, fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=len, mds=50000000, t=type, ns=200, r=radiu, k=5, nw=5, wf=1000, dr=7), time_total_threshold=120000)
            print(data)
        print('\n')

