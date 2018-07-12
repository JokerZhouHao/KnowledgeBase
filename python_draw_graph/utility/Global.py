basePath = "D:\\nowMask\\KnowledgeBase\\sample_result\\"

def getSampleResultPath(type, numSample, radius, k, numWid, prefix):
    #testSampleResultFile.t=1.ns=500.r=3.k=10.nw=10.csv
    if (prefix!=None) and (prefix!=''):
        return basePath + "testSampleResultFile." + prefix + ".t=" + str(type) + ".ns=" + str(numSample) + ".r=" + str(radius) + ".k=" + str(k) + ".nw=" + str(numWid) + ".csv"
    else:
        return basePath + "testSampleResultFile.t=" + str(type) + ".ns=" + str(numSample) + ".r=" + str(radius) + ".k=" + str(k) + ".nw=" + str(numWid) + ".csv"

# print(getSampleResultPath(0, 500, 3, 3, 10))
