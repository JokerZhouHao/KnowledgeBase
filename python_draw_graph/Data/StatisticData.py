from utility import Global

class Data:
    def __init__(self, t=0, ns=500, r=1, k=1, nw=1, prefix=None, f_TYPE_TEST=None, f_nwlen=None, f_mds=None, relativePath=None, fp=None):
        if fp==None:
            self.prefix = prefix
            self.t = t
            self.ns = ns
            self.r = r
            self.k = k
            self.nw = nw
            self.filePath = Global.getSampleResultPath(t, ns, r, k, nw, prefix=prefix, f_TYPE_TEST=f_TYPE_TEST, f_nwlen=f_nwlen, f_mds=f_mds, relativePath=relativePath)
        else:   self.filePath=fp

        self.numSample = 0
        self.timeSemantic = 0
        self.timeOther = 0
        self.timeTotal = 0
        self.numTQSP = 0
        self.numAccessedRTreeNode = 0

    # 计算所需字段的平均值
    @staticmethod
    #testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
    def getData(t=0, ns=500, r=1, k=1, nw=1, prefix=None, f_TYPE_TEST=None, f_nwlen=None, f_mds=None, relativePath=None, fp=None, time_total_threshold=1000000000):
        if fp==None:    data = Data(t, ns, r, k, nw, prefix=prefix, f_TYPE_TEST=f_TYPE_TEST, f_nwlen=f_nwlen, f_mds=f_mds, relativePath=relativePath)
        else:   data = Data(fp=fp)
        index = 0
        with open(data.filePath) as f:
            f.readline()
            while True:
                line = f.readline()
                if line=='':    break
                # print(index, end=' ')
                # print(line, end='')

                # print(index, end=' ')
                strArr = line.split(',')
                single_total_time = int(strArr[32])
                if single_total_time >= time_total_threshold:  continue
                else:
                    index += 1
                # index += 1
                data.numAccessedRTreeNode += int(strArr[25])
                data.numTQSP += int(strArr[27])
                data.timeSemantic += int(strArr[28])
                data.timeTotal += int(strArr[32])
                data.timeOther += (int(strArr[32]) - int(strArr[28]))
        data.numAccessedRTreeNode /= index
        data.numTQSP /= index
        data.timeSemantic /= index
        data.timeTotal /= index
        data.timeOther /= index
        data.numSample = index
        return  data


    # 计算那些编号样例未超时
    @staticmethod
    def get_in_time_simple_indexs(t=0, ns=500, r=1, k=1, nw=1, prefix=None, f_TYPE_TEST=None, f_nwlen=None, f_mds=None, relativePath=None, fp=None, time_total_threshold=1000000000):
        if fp==None:    data = Data(t, ns, r, k, nw, prefix=prefix, f_TYPE_TEST=f_TYPE_TEST, f_nwlen=f_nwlen, f_mds=f_mds, relativePath=relativePath)
        else:   data = Data(fp=fp)
        indexs = set()
        index = 0
        with open(data.filePath) as f:
            f.readline()
            while True:
                line = f.readline()
                if line=='':    break
                index += 1

                strArr = line.split(',')
                single_total_time = int(strArr[32])
                if single_total_time >= time_total_threshold:  continue
                else:
                    indexs.add(index)
        return  indexs

    # 计算确定样本编号平均时间
    @staticmethod
    def getData_by_indexs(indexs, t=0, ns=500, r=1, k=1, nw=1, prefix=None, f_TYPE_TEST=None, f_nwlen=None, f_mds=None, relativePath=None, fp=None, time_total_threshold=1000000000):
        if fp==None:    data = Data(t, ns, r, k, nw, prefix=prefix, f_TYPE_TEST=f_TYPE_TEST, f_nwlen=f_nwlen, f_mds=f_mds, relativePath=relativePath)
        else:   data = Data(fp=fp)
        index = 0
        with open(data.filePath) as f:
            f.readline()
            while True:
                line = f.readline()
                if line=='':    break
                index += 1
                if not(index in indexs):    continue
                strArr = line.split(',')
                data.numAccessedRTreeNode += int(strArr[25])
                data.numTQSP += int(strArr[27])
                data.timeSemantic += int(strArr[28])
                data.timeTotal += int(strArr[32])
                data.timeOther += (int(strArr[32]) - int(strArr[28]))
        index = len(indexs)
        data.numAccessedRTreeNode /= index
        data.numTQSP /= index
        data.timeSemantic /= index
        data.timeTotal /= index
        data.timeOther /= index
        data.numSample = index
        return  data


    def get_info(self):
        return self.filePath

    def __str__(self):
        strs = ''
        strs = strs + self.filePath + '\n'
        strs += 'numSample numAccessedRTreeNode numTQSP timeSemantic timeOther timeTotal\n';
        strs += "%-7.0d%-7.0d%-7.0d%-7.0d%-7.0d%-7.0d"%(self.numSample, self.numAccessedRTreeNode, self.numTQSP, self.timeSemantic, self.timeOther, self.timeTotal) + '\n'
        return strs

# data = Data.getData(0, 500, 1, 1, 5)
# print(data)
# print(Data.getData(0, 500, 3, 10, 5, prefix='nwlen=50'))
# print(Data.getData(0, 500, 3, 10, 5, prefix='nwlen=5000'))
# print(Data.getData(0, 500, 3, 10, 5, prefix='nwlen=-1'))
# print(Data.getData(0, 500, 3, 5, 10))
# print(Data.getData(0, 200, 3, 5, 10))
# print(Data.getData(0, 100, 3, 5, 10))
# print(Data.getData(1, 500, 3, 10, 5, prefix='nwlen=5000'))
# print(Data.getData(1, 500, 3, 10, 5, prefix='nwlen=-1'))
