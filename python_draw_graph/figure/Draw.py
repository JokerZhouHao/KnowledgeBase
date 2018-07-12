import numpy as np
import matplotlib.pyplot as plt
import time
import random
from matplotlib import interactive
from Data.StatisticData import Data

class Bar:
    width = 0.148
    span = 0.05
    spanTotal = width + span
    base_startx = 1 - width/2
    hatchx = 'xxxx'
    ylim = 10000

    def __init__(self, xLabel=None, yLabel=None ,is_stack=False, title=None, xs=None, x_txts=None, ys=None, yscale=None, y_type=None):
        self.xLabel = xLabel
        self.yLabel = yLabel
        self.y_type = y_type
        self.is_stack= is_stack
        if xs==None:    self.xs = [0.5, 1, 2, 3, 4, 5, 6, 7, 7.5]
        else:   self.xs = xs
        if x_txts==None:    self.x_txts = [1.0, 3.0, 5.0, 8.0, 10.0, 15.0, 20.0]
        else: self.x_txts = x_txts
        if ys==None:    self.ys = [0.5, 1, 2, 3, 4, 5, 6, 7, 7.5]
        else:   self.ys = ys
        if yscale==None:    self.yscale='log'
        else: self.yscale = yscale
        self.fig = plt.figure(random.randint(1, 10000))
        if title==None: self.fig.canvas.set_window_title('Test')
        else:   self.fig.canvas.set_window_title(title)
        self.ax = self.fig.add_subplot(111)

    # 画柱状图
    def draw_bar(self, start_index, hs, ys=None, label=None, faceColor='#CCCCCC', hatch=None, linestyle='solid', edgecolor='black'):
        startx = Bar.base_startx + start_index * Bar.spanTotal
        if ys==None:
            ys = [0 for i in range(len(hs))]
        for i in range(len(hs)):
            bxs = []
            bxs.append(startx + i)
            bxs.append(startx + i + Bar.width)
            bxs.append(startx + i + Bar.width)
            bxs.append(startx + i)
            bys = []
            bys.append(ys[i])
            bys.append(ys[i])
            bys.append(ys[i] + hs[i])
            bys.append(ys[i] + hs[i])
            self.ax.fill(bxs, bys, hatch=hatch, fc=faceColor, ls=linestyle, ec=edgecolor, lw=1)
            yPos = 0.72
            if self.yscale != 'log': yPos=-30
            if self.y_type=='TQSP': yPos = -10
            if self.y_type=='RTree': yPos = -1.3
            if self.y_type=='NW': yPos = -35
            if label!=None:
                # self.ax.vlines((startx + i + startx + i + Bar.width)/2, 0, 1.2, colors='k', lw=1.4)
                self.ax.text((startx + i + startx + i + Bar.width)/2, yPos, label, fontsize=9, ha='center', rotation=90)

    def show(self):

        if self.is_stack:
            self.ax.fill([-1, 0, 0, -1], [-1, -1, -1.1, -1.1], label='Other Time', fc='white', ec='black')
            self.ax.fill([-1, 0, 0, -1], [-1, -1, -1.1, -1.1], hatch=Bar.hatchx, label='Semantic Time', fc='#CCCCCC', ec='black')
            self.ax.legend(loc=2, frameon=False, prop={'size': 9})

        self.ax.tick_params(direction='in')
        self.ax.set_xticks(self.xs)
        self.ax.set_xlim(self.xs[0], self.xs[-1])
        if self.yscale=='log':
            self.ax.set_ylim(1, Bar.ylim)
            self.ax.set_yscale('log')
        else:
            self.ax.set_yticks(self.ys)
            self.ax.set_ylim(self.ys[0], self.ys[-1])

        # 设置下方x轴标题位置
        xLabel_x = 3.8
        xLabel_y = 0.3
        if self.yscale!='log':  xLabel_y -= 120
        if self.y_type=='TQSP': xLabel_y = -10 - 28
        if self.y_type=='RTree': xLabel_y = -1.3 - 3.8
        if self.y_type=='NW':
            xLabel_x = 3
            xLabel_y = -35 - 100

        if self.xLabel!=None:
            self.ax.text(xLabel_x, xLabel_y, self.xLabel)
        if self.yLabel!=None:   self.ax.set_ylabel(self.yLabel)

        # 隐藏下方x轴的label
        xaxis = self.ax.get_xaxis()
        x_labels = xaxis.get_ticklabels()
        for i in range(len(x_labels)):
            x_labels[i].set_visible(False)
        xaxis.set_ticklabels(x_labels)
        # 隐藏下方x轴的刻度线
        x_lines = xaxis.get_ticklines()
        for ln in x_lines:
            ln.set_visible(False)

        self.ax_x = self.ax.twinx()
        if self.yscale=='log':
            self.ax_x.set_ylim(1, Bar.ylim)
            self.ax_x.set_yscale('log')
        else:
            self.ax_x.set_yticks(self.ys)
            self.ax_x.set_ylim(self.ys[0], self.ys[-1])

        self.ax_y = self.ax.twiny()
        self.ax_y.tick_params(direction='in')
        self.ax_y.set_xticks(self.xs)
        self.ax_y.set_xlim(self.xs[0], self.xs[-1])

        # self.fig.canvas.draw()
        # 改变上方x轴的labels
        xaxis = self.ax_y.get_xaxis()
        x_labels = xaxis.get_ticklabels()
        for i in range(len(x_labels)):
            if i==0 or i==len(x_labels)-1:
                x_labels[i].set_visible(False)
            else:
                x_labels[i].set_text(self.x_txts[i-1])
        xaxis.set_ticklabels(x_labels)
        interactive(True)
        plt.show()

    @staticmethod
    def sleep():
        plt.pause(1200)

    # 绘制 top-k 的图
    # type=0【统计runtime】 1【统计TQSP】 2【R-tree nodes accessed】
    @staticmethod
    def draw_topK(type=0):
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        ts = [0, 1]
        ns = 500
        radius = 3
        ks = [1, 3, 5, 8, 10, 15, 20]
        nw = 5

        if type==0:
            ys = [i*100 for i in range(13)]
            y_label = 'Runtime (ms)'
        elif type==1:
            ys = [i*100 for i in range(5)]
            y_label = '# of TQSP Computations'
        else:
            ys = [i*10 for i in range(6)]
            y_label = '# of R-tree nodes accessed'

        if type==0:
            timeBar = Bar(xLabel=r'top-$k$', yLabel=y_label, is_stack=True, title=r'top-k test', ys=ys, yscale='linear')
            for t in ts:
                datas = []
                for k in ks:
                    data = Data.getData(t, ns, radius, k, nw)
                    print(data)
                    datas.append(data)
                if t==0: tit = 'SD'
                else: tit = 'RD'
                timeOthers = []
                timeSemantics = []
                for data in datas:
                    timeSemantics.append(data.timeSemantic)
                    timeOthers.append(data.timeOther)
                timeBar.draw_bar(t, timeSemantics, hatch=Bar.hatchx, label=tit)
                timeBar.draw_bar(t, timeOthers, ys=timeSemantics, faceColor='white')
            timeBar.show()
        else:
            if 1==type: y_type = 'TQSP'
            elif 2==type: y_type = 'RTree'
            bar = Bar(xLabel=r'top-$k$', yLabel=y_label, title='top-k test', ys=ys, yscale='linear', y_type=y_type)
            for t in ts:
                datas = []
                for k in ks:
                    data = Data.getData(t, ns, radius, k, nw)
                    print(data)
                    datas.append(data)
                if t==0: tit = 'SD'
                else: tit = 'RD'
                list1 = []
                for data in datas:
                    if 1==type:
                        list1.append(data.numTQSP)
                    elif 2==type:
                        list1.append(data.numAccessedRTreeNode)
                bar.draw_bar(t, list1, label=tit)
            bar.show()


    # 绘制查询词的图
    @staticmethod
    def draw_n_words():
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        xs = [0.5, 1, 2, 3, 4, 5, 5.5]
        x_txts = [1.0, 3.0, 5.0, 8.0, 10.0]
        ys = [i*100 for i in range(15)]
        ts = [0, 1]
        ns = 500
        radius = 3
        k = 5
        nws = [1, 3, 5, 8, 10]

        timeBar = Bar(xLabel=r'|$q.\psi$|', yLabel='Runtime (ms)', is_stack=True, title='n_wrods test', ys=ys, yscale='linear', xs=xs, x_txts=x_txts, y_type='NW')
        for t in ts:
            datas = []
            for nw in nws:
                data = Data.getData(t, ns, radius, k, nw)
                print(data)
                datas.append(data)
            if t==0: tit = 'SD'
            else: tit = 'RD'
            timeOthers = []
            timeSemantics = []
            for data in datas:
                timeSemantics.append(data.timeSemantic)
                timeOthers.append(data.timeOther)
            timeBar.draw_bar(t, timeSemantics, hatch=Bar.hatchx, label=tit)
            timeBar.draw_bar(t, timeOthers, ys=timeSemantics, faceColor='white')
        timeBar.show()

# 折线图
class LineChart:
    ylim = 10000
    line_types = ["s", "|", "v", "x", 'D', "^", 'o', ]
    line_type_index = 0
    line_color = 'black'

    def __init__(self, xs, x_txts, xLabel=None, yLabel=None ,title=None, ys=None, yscale=None, y_type=None):
        self.xLabel = xLabel
        self.yLabel = yLabel
        self.y_type = y_type
        if xs==None:    self.xs = [0.5, 1, 2, 3, 4, 5, 6, 7, 7.5]
        else:   self.xs = xs
        if x_txts==None:    self.x_txts = [1.0, 3.0, 5.0, 8.0, 10.0, 15.0, 20.0]
        else: self.x_txts = x_txts
        if ys==None:    self.ys = [0.5, 1, 2, 3, 4, 5, 6, 7, 7.5]
        else:   self.ys = ys
        if yscale==None:    self.yscale='log'
        else: self.yscale = yscale
        self.fig = plt.figure(random.randint(1, 10000))
        if title==None: self.fig.canvas.set_window_title('Test')
        else:   self.fig.canvas.set_window_title(title)
        self.ax = self.fig.add_subplot(111)

    # 画折线图
    def draw_line(self, ys, label):
        self.ax.plot(self.xs, ys, label=label, marker=self.line_types[self.line_type_index])
        self.line_type_index += 1

    def show(self):
        self.ax.legend(loc=1, prop={'size': 9})

        self.ax.tick_params(direction='in')
        self.ax.set_xticks(self.xs)
        self.ax.set_xlim(self.xs[0], self.xs[-1])

        # 设置y轴的刻度
        if self.yscale=='log':
            self.ax.set_ylim(1, Bar.ylim)
            self.ax.set_yscale('log')
        else:
            self.ax.set_yticks(self.ys)
            self.ax.set_ylim(self.ys[0], self.ys[-1])

        xaxis = self.ax.get_xaxis()
        # 隐藏下方x轴的刻度线
        # x_lines = xaxis.get_ticklines()
        # for ln in x_lines:
        #     ln.set_visible(False)

        # 设置下方x轴的label
        x_labels = xaxis.get_ticklabels()
        for i in range(len(x_labels)):
            x_labels[i].set_text(self.x_txts[i])
        xaxis.set_ticklabels(x_labels)

        # 设置下方x轴标题位置
        xLabel_x = (self.xs[0] + self.xs[-1])/2
        xLabel_y = -0.5
        if self.xLabel!=None:
            # self.ax.text(xLabel_x, xLabel_y, self.xLabel)
            self.ax.set_xlabel(self.xLabel)
        if self.yLabel!=None:   self.ax.set_ylabel(self.yLabel)

        # 添加右边y轴的刻度
        self.ax_x = self.ax.twinx()
        if self.yscale=='log':
            self.ax_x.set_ylim(1, Bar.ylim)
            self.ax_x.set_yscale('log')
        else:
            self.ax_x.set_yticks(self.ys)
            self.ax_x.set_ylim(self.ys[0], self.ys[-1])

        interactive(True)
        plt.show()

    # 画radius的折线图
    @staticmethod
    def draw_radius():
        ys = [600+i*40 for i in range(0, 16)]
        xs=[i for  i in  range(0, 3)]
        x_txts = [1.0, 2.0, 3.0]
        chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=r'$\alpha$-radius', yLabel='Runtime (ms)')

        radius = [1, 2, 3]
        ks = [1, 3, 5, 8, 10, 15, 20]
        for k in ks:
            runtimes = []
            for r in radius:
                data = Data.getData(0, 500, r, k, 5)
                runtimes.append(data.timeTotal)
            chart.draw_line(runtimes, 'k=' + str(k))
        chart.show()

    @staticmethod
    def sleep():
        plt.pause(1200)

LineChart.draw_radius()
# LineChart.sleep()





# ys = [100*i for i in range(0, 12)]
# xs=[i for  i in  range(0, 4)]
# x_txts = [1.0, 2.0, 3.0, 5.0]
# chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel='a-radius', yLabel='Runtime (ms)')
#
# ys1 = np.array([10, 30, 50, 70])
# chart.draw_line(ys1, 'k=1')
#
# ys2 = ys1 + 100;
# chart.draw_line(ys2, 'k=2')
#
# ys3 = ys2 + 100;
# chart.draw_line(ys3, 'k=3')
#
# ys4 = ys3 + 100;
# chart.draw_line(ys4, 'k=4')
#
# ys5 = ys4 + 100;
# chart.draw_line(ys5, 'k=5')
#
# ys6 = ys5 + 100;
# chart.draw_line(ys6, 'k=6')
#
# ys7 = ys6 + 100;
# chart.draw_line(ys7, 'k=7')
#
# chart.show()
# chart.sleep()







Bar.draw_topK()
Bar.draw_topK(1)
Bar.draw_topK(2)
Bar.draw_n_words()

Bar.sleep()


# bar = Bar(xLabel='top-k', yLabel='# of TQSP Computations')

# bar = Bar(xLabel='top-k', yLabel='# of TQSP Computations', is_stack=True)
#
# hs0 = [100, 1000]
# hs00 = [100, 1000]
# ys00 = hs0
#
# hs1 = [100, 1000]
#
# hs2 = [100, 1000]
#
# bar.draw_bar(0, hs0, label='BSP', hatch=Bar.hatchx)
# bar.draw_bar(0, hs00, ys=ys00, faceColor='white')
#
# # bar.draw_bar(0, hs1, label='SPP')
# # bar.draw_bar(1, hs2, label='SP')
# bar.show()
#
# bar1 = Bar(xLabel='top-k', yLabel='# of TQSP Computations', is_stack=True)
# bar1.draw_bar(2, hs0, label='BSP', hatch=Bar.hatchx)
# bar1.draw_bar(2, hs00, ys=ys00, faceColor='white')
# bar1.show()
#
# bar.sleep()
