basePath = "D:\\nowMask\\KnowledgeBase\\sample_result\\"

TYPE_TEST = 'SPBest'
nwlen = 1000000
mds = 1000
def getSampleResultPath(type, numSample, radius, k, numWid, prefix=None, f_TYPE_TEST=None, f_nwlen=None, f_mds=None, relativePath = None):
    #testSampleResultFile.t=1.ns=500.r=3.k=10.nw=10.csv
    if relativePath==None:  relativePath=''
    else:   relativePath += '\\'
    if (prefix==''):
        return basePath + relativePath + "testSampleResultFile.t=" + str(type) + ".ns=" + str(numSample) + ".r=" + str(radius) + ".k=" + str(k) + ".nw=" + str(numWid) + ".csv"
    if (prefix!=None):
        return basePath + relativePath + "testSampleResultFile." + prefix + ".t=" + str(type) + ".ns=" + str(numSample) + ".r=" + str(radius) + ".k=" + str(k) + ".nw=" + str(numWid) + ".csv"

    prefix = ''
    if f_TYPE_TEST!=None:
        prefix += f_TYPE_TEST + '.'
    else:
        prefix += TYPE_TEST + '.'

    if f_nwlen!=None:
        prefix += 'nwlen=' + str(f_nwlen) + '.'
    else:
        prefix += 'nwlen=' + str(nwlen) + '.'

    if f_mds!=None:
        prefix += 'mds=' + str(f_mds) + '.'
    else:
        prefix += 'mds=' + str(mds) + '.'
    return basePath  + relativePath +  "testSampleResultFile" + '.' + \
                    prefix + \
                    "t=" + str(type) + ".ns=" + str(numSample) + ".r=" + str(radius) + ".k=" + str(k) + ".nw=" + str(numWid) + ".csv"

# print(getSampleResultPath(0, 500, 3, 10, 10))
# print(getSampleResultPath(0, 500, 3, 3, 10, f_TYPE_TEST='kk', f_nwlen=20, f_mds=30))
# print(getSampleResultPath(0, 500, 3, 5, 10, relativePath='SPBest_nwlen=1000000_mds=unknow', f_nwlen=1111, f_mds=12))
