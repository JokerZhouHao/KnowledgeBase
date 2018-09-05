from utility import Global
#testSampleResultFile.SPBest.nwlen=1000000.mds=1000.t=0.ns=200.r=2.k=5.nw=5.wf=0

input_path = "D:\\nowMask\\KnowledgeBase\\data\\DataSet\\DBpedia\\orginal\\"
output_path = "D:\\nowMask\\KnowledgeBase\\data\\DataSet\\DBpedia\\orginalIndex\\"

def sample_res_path(dir, sp='SPBest', nwlen=1000000, mds=1000, t=0, ns=200, r=2, k=5, nw=5, wf=None, dr=None):
    # testSampleResultFile.SPBest.nwlen=1000000.mds=1000.t=0.ns=200.r=2.k=5.nw=5.wf=0
    fp = dir + 'testSampleResultFile';
    fp += '.' + sp
    fp += '.nwlen=' + str(nwlen)
    fp += '.mds=' + str(mds)
    fp += '.t=' + str(t)
    fp += '.ns=' + str(ns)
    fp += '.r=' + str(r)
    fp += '.k=' + str(k)
    fp += '.nw=' + str(nw)
    if wf!=None:
        fp += '.wf=' + str(wf)
    if dr!=None:
        fp += '.dr=' + str(dr)
    return fp+'.csv'

def figure_path():
    return '..\\Data\\figures\\'

# base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\'
# print(sample_res_path(base_dir, 'SPBest', 1000, 4, 0, 200, 2, 5, 5, 3, 3))
