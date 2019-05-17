import numpy as np
import matplotlib.pyplot as plt
import time
import random
from matplotlib import interactive
from Data.StatisticData import Data
from utility import PathUtility
from utility import Global

BAR_FONT_SIZE = 25

class Bar:
    width = 0.148
    span = 0.05
    spanTotal = width + span
    base_startx = 1 - width/2*3 - span
    hatchx = 'xx'
    hatchxes = ['xx', '.', '+', 'O']
    ylim = 1000000

    def __init__(self, xLabel=None, yLabel=None ,is_stack=False, title=None, xs=None, x_txts=None, ys=None, yscale=None, y_type=None, f_type=None, fpath='test.pdf'):
        self.xLabel = xLabel
        self.yLabel = yLabel
        self.y_type = y_type
        self.is_stack= is_stack
        if xs==None:    self.xs = [0.5, 1, 2, 3, 4, 5, 6, 7, 7.5]
        else:   self.xs = xs
        if x_txts==None:    self.x_txts = [1.0, 3.0, 5.0, 8.0, 10.0, 15.0, 20.0]
        else: self.x_txts = x_txts
        if ys==None:    self.ys = [i**10 for i in  range(5)]
        else:   self.ys = ys
        if yscale==None:    self.yscale='log'
        else: self.yscale = yscale
        self.fig = plt.figure(random.randint(1, 10000), figsize=(10.1023, 6.5), tight_layout=True)
        if title==None: self.fig.canvas.set_window_title('Test')
        else:   self.fig.canvas.set_window_title(title)
        self.f_type = f_type
        plt.rcParams['font.size'] = 25
        self.ax = self.fig.add_subplot(111)
        self.fpath = fpath

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
            if i==0:
                self.ax.fill(bxs, bys, hatch=hatch, fc=faceColor, ls=linestyle, ec=edgecolor, lw=1, label=label)
            else:
                self.ax.fill(bxs, bys, hatch=hatch, fc=faceColor, ls=linestyle, ec=edgecolor, lw=1)

            yPos = self.ys[0] - 11
            if self.f_type=='radius_len_SPTD*':
                yPos = self.ys[0] - 13
            elif self.f_type=='radius_len_SPTR*':
                yPos = self.ys[0] - 14

            # if self.yscale != 'log': yPos=-30
            # if self.y_type=='TQSP': yPos = 0.7
            # if self.y_type=='RTree': yPos = 0.7
            # if self.y_type=='NW': yPos = -35

            # if label!=None:
            #     self.ax.text((startx + i + startx + i + Bar.width)/2, yPos, label, fontsize=20, ha='center', rotation=0)

    def show(self):
        # 设置legend
        if self.is_stack:
            self.ax.fill([-1, 0, 0, -1], [-1, -1, -1.1, -1.1], label='Other Time', fc='white', ec='black')
            self.ax.fill([-1, 0, 0, -1], [-1, -1, -1.1, -1.1], hatch=Bar.hatchx, label='Semantic Time', fc='#CCCCCC', ec='black')
            self.ax.legend(loc=2, frameon=False)
        elif self.f_type.startswith('alpha_len_SPTD'):
            # self.ax.legend(loc=2, frameon=False, prop={'size': 25})
            self.ax.legend(loc=1, frameon=False)
        elif self.f_type.startswith('alpha_len_SPTR'):
            # self.ax.legend(loc=2, frameon=False, prop={'size': 25})
            self.ax.legend(loc=1, frameon=False)

        # 设置X、Y限制
        self.ax.tick_params(axis='x', direction='in', width=3, length=8, which='major')
        self.ax.tick_params(axis='y', direction='out', width=3, length=8, which='major')
        self.ax.tick_params(direction='out', width=2, length=4, which='minor')
        self.ax.set_xticks(self.xs)
        self.ax.set_xlim(self.xs[0], self.xs[-1])
        if self.yscale=='log':
            self.ax.set_ylim(1, Bar.ylim)
            self.ax.set_yscale('log')
        else:
            self.ax.set_yticks(self.ys)
            self.ax.set_ylim(self.ys[0], self.ys[-1])

        # 设置下方X、Y轴标题位置
        xLabel_x = (self.xs[0] + self.xs[-1]/5*4)/2;
        # xLabel_x = 3
        xLabel_y = 0.3
        # xLabel_y = 1
        if self.yscale!='log':  xLabel_y -= 120
        if self.y_type=='TQSP': xLabel_y = 0.5
        if self.y_type=='RTree': xLabel_y = 0.3
        # if self.y_type=='NW':
        #     xLabel_x = 3
        #     xLabel_y = -35 - 100
        # if self.xLabel!=None:
            # self.ax.text(xLabel_x, xLabel_y, self.xLabel)
            # self.ax.set_xlabel(self.xLabel, va='bottom', visible=True)
            # self.ax.set_title(self.xLabel, fontsize=9, verticalalignment='bottom', pad=17)
        if self.yLabel!=None:   self.ax.set_ylabel(self.yLabel)
        if self.xLabel != None:  self.ax.set_xlabel(self.xLabel)

        # 隐藏下方x轴的label
        yPos = -0.008
        if self.f_type.startswith('radius_len'):
            yPos = -0.1
        xaxis = self.ax.get_xaxis()
        x_labels = xaxis.get_ticklabels()
        for i in range(len(x_labels)):
            if i==0 or i==len(x_labels)-1:
                x_labels[i].set_visible(False)
            else:
                pos = x_labels[i].get_position()
                print(pos)
                print(x_labels[i].get_verticalalignment())
                if yPos!=None:
                    pos = (pos[0], yPos)
                x_labels[i].set_position(pos)
                x_labels[i].set_verticalalignment('top')
                x_labels[i].set_text(self.x_txts[i-1])
        xaxis.set_ticklabels(x_labels)
        # 隐藏下方x轴的刻度线
        x_lines = xaxis.get_ticklines()
        for ln in x_lines:
            ln.set_visible(False)

        # 设置右边Y轴刻度
        # self.ax_x = self.ax.twinx()
        # right_y_axis = self.ax_x.get_yaxis()
        # yticks = right_y_axis.get_ticklabels()
        # for tk in yticks:
        #     tk.set_visible(False)
        # right_y_axis.set_ticklabels(yticks)

        # if self.yscale=='log':
        #     self.ax_x.set_ylim(1, Bar.ylim)
        #     self.ax_x.set_yscale('log')
        # else:
        #     self.ax_x.set_yticks(self.ys)
        #     self.ax_x.set_ylim(self.ys[0], self.ys[-1])

        # 设置上方X轴刻度
        # self.ax_y = self.ax.twiny()
        #
        # if self.xLabel != None:
        #     self.ax_y.set_xlabel(self.xLabel)
        #
        # self.ax_y.tick_params(axis='x', direction='in', width=3, length=8, which='major')
        # self.ax_y.set_xticks(self.xs)
        # self.ax_y.set_xlim(self.xs[0], self.xs[-1])
        #
        # # 改变上方x轴的labels
        # xaxis = self.ax_y.get_xaxis()
        # # 替换刻度
        # x_labels = xaxis.get_ticklabels()
        # for i in range(len(x_labels)):
        #     if i==0 or i==len(x_labels)-1:
        #         x_labels[i].set_visible(False)
        #     else:
        #         x_labels[i].set_text(self.x_txts[i-1])
        # xaxis.set_ticklabels(x_labels)
        # # 隐藏刻度线
        # x_lines = xaxis.get_ticklines()
        # for i in range(len(x_lines)):
        #     if i==1 or i==len(x_labels)*2-1:
        #         x_lines[i].set_visible(False)

        # self.ax.grid('on')

        # 设置字体大小
        # plt.xticks(fontsize=25)
        # plt.yticks(fontsize=25)

        interactive(True)
        plt.show()
        self.fig.savefig(self.fpath)

    @staticmethod
    def sleep():
        plt.pause(1200)

    # 绘制 top-k 的图
    # type=0【统计runtime】 1【统计TQSP】 2【R-tree nodes accessed】
    @staticmethod
    def draw_topK(search_type=0, bar_type=0):
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        alg_type = ['SPBase', 'SPBest']
        ns = 200
        radius = 2
        ks = [1, 3, 5, 8, 10, 15, 20]
        nw = 5

        if bar_type==0:
            ys = [i*1000 for i in range(13)]
            ys = None
            y_label = 'Runtime (ms)'
        elif bar_type==1:
            ys = [i*100 for i in range(5)]
            y_label = '# of TQSP Computations'
        else:
            ys = [i*10 for i in range(6)]
            y_label = '# of R-tree nodes accessed'

        base_dir = 'D:\\nowMask\KnowledgeBase\\sample_result\\yago2s_single_date\\k_nw\\'

        if bar_type==0:
            timeBar = Bar(xLabel=r'top-$k$', yLabel=y_label, is_stack=True, title=r'top-k type=' + str(search_type), ys=ys)
            for i in range(len(alg_type)):
                datas = []
                for k in ks:
                    data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_type[i], nwlen=1000000, mds=1000, t=search_type, ns=200, r=2, k=k, nw=5, wf=500, dr=7))
                    print(data)
                    datas.append(data)
                timeOthers = []
                timeSemantics = []
                for data in datas:
                    timeSemantics.append(data.timeSemantic)
                    timeOthers.append(data.timeOther)

                timeBar.draw_bar(i, timeSemantics, hatch=Bar.hatchx, label=alg_type[i])
                timeBar.draw_bar(i, timeOthers, ys=timeSemantics, faceColor='white')
            timeBar.show()
        else:
            if 1==bar_type: y_type = 'TQSP'
            elif 2==bar_type: y_type = 'RTree'
            bar = Bar(xLabel=r'top-$k$', yLabel=y_label, title=r'top-k type=' + str(search_type), ys=ys, y_type=y_type)
            for i in range(len(alg_type)):
                datas = []
                for k in ks:
                    data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_type[i], nwlen=1000000, mds=1000, t=search_type, ns=200, r=2, k=k, nw=5, wf=500, dr=7))
                    print(data)
                    datas.append(data)
                list1 = []
                for data in datas:
                    if 1==bar_type:
                        list1.append(data.numTQSP)
                    elif 2==bar_type:
                        list1.append(data.numAccessedRTreeNode)
                bar.draw_bar(i, list1, label=alg_type[i])
            bar.show()

    # 绘制查询词的图
    @staticmethod
    def draw_n_words(search_type=0):
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        xs = [0.5, 1, 2, 3, 4, 5, 5.5]
        x_txts = [1.0, 3.0, 5.0, 8.0, 10.0]
        ys = [i*100 for i in range(15)]
        alg_type = ['SPBase', 'SPBest']
        ns = 200
        radius = 2
        k = 5
        nws = [1, 3, 5, 8, 10]

        base_dir = 'D:\\nowMask\KnowledgeBase\\sample_result\\yago2s_single_date\\k_nw\\'

        timeBar = Bar(xLabel=r'|$q.\psi$|', yLabel='Runtime (ms)', is_stack=True, title='n_wrods type=' + str(search_type), ys=ys, xs=xs, x_txts=x_txts, y_type='NW')
        for i in range(len(alg_type)):
            datas = []
            for nw in nws:
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_type[i], nwlen=1000000, mds=1000, t=search_type, ns=200, r=2, k=5, nw=nw, wf=500, dr=7))
                print(data)
                datas.append(data)
            timeOthers = []
            timeSemantics = []
            for data in datas:
                timeSemantics.append(data.timeSemantic)
                timeOthers.append(data.timeOther)
            timeBar.draw_bar(i, timeSemantics, hatch=Bar.hatchx, label=alg_type[i])
            timeBar.draw_bar(i, timeOthers, ys=timeSemantics, faceColor='white')
        timeBar.show()

    # 绘制查询词的图
    @staticmethod
    def draw_radius_len(search_type=0, base_y = 600, ftype=None, fpath='test.pdf'):
        xs = [0.5, 1, 2, 3, 3.5]
        x_txts = [r'$10^5$', r'$10^6$', r'$10^7$']

        if search_type==0:
            ys = [5000+i*500 for i in range(0, 5)]
        elif search_type==1:
            ys = [1000+i*1000 for i in range(0, 5)]

        if search_type==0:
            title = 'SPTD*'
        elif search_type==1:
            title = 'SPTR*'

        timeBar = Bar(xLabel=r'$\mathit{l}$', yLabel='Runtime (ms)', is_stack=False, title=title, ys=ys, xs=xs, x_txts=x_txts, yscale='linear', y_type='NW', f_type=ftype, fpath=fpath)

        base_dir = Global.baseYagoSamplePath + 'radius_len_nw=3\\'
        radius = [1, 2, 3]
        radius_txt = [r'$\alpha$-radius=1', r'$\alpha$-radius=2', r'$\alpha$-radius=3']
        lens = (100000, 1000000, 10000000)

        for radiu_i in range(len(radius)):
            runtimes = []
            for len_i in range(len(lens)):
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=lens[len_i], mds=50000000, t=search_type, ns=200, r=radius[radiu_i], k=5, nw=3, wf=50, dr=7), time_total_threshold=12000000)
                print(data)
                runtimes.append(data.timeTotal)
            timeBar.draw_bar(radiu_i, runtimes, hatch=Bar.hatchxes[radiu_i], label=radius_txt[radiu_i], )
            # timeBar.draw_bar(radiu_i, runtimes, label=radius[radiu_i])
        timeBar.show()

    # 绘制不同规模柱状图
    @staticmethod
    def draw_differ_size(search_type=0, bar_type=0):
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        xs = [0.5, 1, 2, 3, 4, 4.5]
        x_txts = [2.0, 4.0, 6.0, 8.0]

        alg_type = ['SPBase', 'SPBest']
        ns = 200
        radius = 2
        sizes = [2, 4, 6, 8]
        nw = 5
        k = 5

        if bar_type==0:
            ys = [i*1000 for i in range(13)]
            ys = None
            y_label = 'Runtime (ms)'
        elif bar_type==1:
            ys = [i*100 for i in range(5)]
            y_label = '# of TQSP Computations'
        else:
            ys = [i*10 for i in range(6)]
            y_label = '# of R-tree nodes accessed'

        if bar_type==0:
            timeBar = Bar(xLabel='Graph Vertex Size (in million)', yLabel=y_label, is_stack=True, title='Graph Vertex Size type=' + str(search_type), ys=ys, xs=xs, x_txts=x_txts)
            for i in range(len(alg_type)):
                datas = []
                for size in sizes:
                    dir_name = 'sample_result';
                    if size!=8:   dir_name = dir_name + '_' + str(int(size) * 1000000)
                    dir_name = dir_name + '\\'
                    data = Data.getData(search_type, ns, radius, k, nw, relativePath='nw=10\\differ_size\\' + dir_name, prefix=alg_type[i]+'.nwlen=1000000.mds=1000')
                    print(data)
                    datas.append(data)
                timeOthers = []
                timeSemantics = []
                for data in datas:
                    timeSemantics.append(data.timeSemantic)
                    timeOthers.append(data.timeOther)

                timeBar.draw_bar(i, timeSemantics, hatch=Bar.hatchx, label=alg_type[i])
                timeBar.draw_bar(i, timeOthers, ys=timeSemantics, faceColor='white')
            timeBar.show()
        else:
            if 1==bar_type: y_type = 'TQSP'
            elif 2==bar_type: y_type = 'RTree'
            bar = Bar(xLabel='Graph Vertex Size (in million)', yLabel=y_label, title='Graph Vertex Size type=' + str(search_type), ys=ys, y_type=y_type, xs=xs, x_txts=x_txts)
            for i in range(len(alg_type)):
                datas = []
                for size in sizes:
                    dir_name = 'sample_result';
                    if size!=8:   dir_name = dir_name + '_' + str(int(size) * 1000000)
                    dir_name = dir_name + '\\'
                    data = Data.getData(search_type, ns, radius, k, nw, relativePath='nw=10\\differ_size\\' + dir_name, prefix=alg_type[i]+'.nwlen=1000000.mds=1000')
                    print(data)
                    datas.append(data)
                list1 = []
                for data in datas:
                    if 1==bar_type:
                        list1.append(data.numTQSP)
                    elif 2==bar_type:
                        list1.append(data.numAccessedRTreeNode)
                bar.draw_bar(i, list1, label=alg_type[i])
            bar.show()

# 折线图
class LineChart:
    ylim = 10000
    line_types = ["$\\bigcirc$", "$+$", "$\\bigtriangledown$", "$\\ast$", '$\\diamondsuit$', "$\\bigtriangleup$", '$\\times$', "<", ">", "H"]
    line_type_index = 0
    line_color = 'black'

    def __init__(self, xs, x_txts, xLabel=None, yLabel=None ,title=None, ys=None, yscale=None, ylim=None, y_type=None, xlabel_rotation=0, fpath='test.pdf'):
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
        self.fig = plt.figure(random.randint(1, 10000), figsize=(10.1023, 6.5), tight_layout=True)
        if title==None: self.fig.canvas.set_window_title('Test')
        else:   self.fig.canvas.set_window_title(title)
        self.xlabel_rotation = xlabel_rotation
        plt.rcParams['font.size'] = 20
        self.ax = self.fig.add_subplot(111)
        self.ylim = ylim
        self.fpath = fpath

    # 画折线图
    def draw_line(self, ys, label):
        self.ax.plot(self.xs, ys, label=label, marker=self.line_types[self.line_type_index], c='black', markersize=18)
        self.line_type_index += 1

    def show(self):
        plt.rcParams['font.size'] = 25
        if self.yLabel.find('TQTSP') != -1:
            # self.ax.legend(loc=1, prop={'size': 15}, bbox_to_anchor=(0.97, 0.84))
            self.ax.legend(loc=2, prop={'size': 15})
        if self.yLabel.find('Runtime') != -1 and self.xLabel.find('top') != -1:
            self.ax.legend(loc=1, prop={'size': 13}, bbox_to_anchor=(0.18, 1.018))
            # self.ax.legend(loc=1, prop={'size': 15}, bbox_to_anchor=(0.2, 1.1))
        elif self.xLabel.find('top') != -1 and self.yLabel.find('Runtime') != -1:
            self.ax.legend(loc=2, prop={'size': 15})
        elif self.xLabel.find('psi') != -1:
            self.ax.legend(loc=1, prop={'size': 13}, bbox_to_anchor=(0.4, 1.01))
        else:
            self.ax.legend(loc=2, prop={'size': 15})

        # 设置下方X轴刻度
        self.ax.tick_params(axis='x', direction='in', width=3, length=8, which='major')
        self.ax.tick_params(axis='y', direction='out', width=3, length=8, which='major')
        self.ax.tick_params(direction='out', width=2, length=4, which='minor')
        self.ax.set_xticks(self.xs)
        self.ax.set_xlim(self.xs[0], self.xs[-1])

        # 设置Y轴的刻度
        if self.yscale=='log':
            if self.ylim != None:
                self.ax.set_ylim(self.ylim)
            else:   self.ax.set_ylim(1, Bar.ylim)
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
            x_labels[i].set_rotation(self.xlabel_rotation)
        xaxis.set_ticklabels(x_labels)

        # 设置下方x轴标题位置
        xLabel_x = (self.xs[0] + self.xs[-1])/2
        xLabel_y = -0.5
        if self.xLabel!=None:
            # self.ax.text(xLabel_x, xLabel_y, self.xLabel)
            # self.ax.set_xlabel(self.xLabel)
            # self.ax.set_title(self.xLabel, fontsize=20, verticalalignment='bottom')
            self.ax.set_xlabel(self.xLabel, fontsize=20)
        if self.yLabel!=None:   self.ax.set_ylabel(self.yLabel)

        # 添加右边y轴的刻度
        # self.ax_x = self.ax.twinx()
        # self.ax_x.tick_params(axis='y', direction='out', width=3, length=8, which='major')
        # self.ax_x.tick_params(direction='out', width=2, length=4, which='minor')
        #
        # if self.yscale=='log':
        #     self.ax_x.set_ylim(1, Bar.ylim)
        #     self.ax_x.set_yscale('log')
        # else:
        #     self.ax_x.set_yticks(self.ys)
        #     self.ax_x.set_ylim(self.ys[0], self.ys[-1])

        interactive(True)
        plt.show()
        self.fig.savefig(self.fpath)


    # 画k的折线图
    @staticmethod
    def draw_k(show_type, base_y=None, fpath='test.pdf'):
        ys = None
        yLabel = None
        yscale = 'liner'
        ylim = None

        if show_type==0:
            yLabel = 'Runtime (ms)'
            yscale = 'log'
            ylim = (1000, 100000)
        elif show_type==1:
            yLabel = '# of TQTSP Computations'
            yscale='log'
            ylim = (100, 100000)
            ys = [base_y+i*100 for i in range(0, 11)]
        elif show_type==2:
            yLabel = '# of R-tree nodes accessed'
            ys = [500 + i*500 for i in range(5)]

        x_txts = [1.0, 3.0, 5.0, 8.0, 10.0, 15.0, 20.0]
        xs=[i for  i in  range(len(x_txts))]

        chart = LineChart(xs, x_txts, ys=ys, yscale=yscale, ylim=ylim, xLabel=r'top-$k$', yLabel=yLabel, title=r'top-$k$', fpath=fpath)

        alg_types = ['SPBase', 'SPBest']
        search_types = [0, 1]
        type = None

        ks = [1, 3, 5, 8, 10, 15, 20]
        base_dir = Global.baseYagoSamplePath + 'k_nw_dr\\'
        for search_index in range(len(search_types)):
            for alg_index in range(len(alg_types)):
                if alg_index==0:
                    if search_index==0: type=r'$SPTD$'
                    else:   type=r'$SPTR$'
                else:
                    if search_index==0: type=r'$SPTD^*$'
                    else:   type=r'$SPTR^*$'
                runtimes = []
                for k in ks:
                    data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_types[alg_index], nwlen=10000000, mds=50000000, t=search_types[search_index], ns=200, r=2, k=k, nw=3, wf=1000, dr=7))
                    print(data)
                    if 0==show_type: runtimes.append(data.timeTotal)
                    elif 1==show_type: runtimes.append(data.numTQSP)
                    elif 2==show_type: runtimes.append(data.numAccessedRTreeNode)
                chart.draw_line(runtimes, type)
        chart.show()

    # 画nw的折线图
    @staticmethod
    def draw_nw(base_y=None, fpath='test.pdf'):
        ys = None
        if base_y != None:
            ys = [base_y+i*100 for i in range(0, 11)]
        x_txts = [1.0, 2.0, 3.0, 4.0, 5.0]
        xs=[i for  i in  range(len(x_txts))]

        yLabel = 'Runtime (ms)'

        chart = LineChart(xs, x_txts, ys=ys, ylim=(1000, 100000), xLabel=r'|$q.\psi$|', yLabel=yLabel, title=r'|$q.\psi$|', fpath=fpath)

        alg_types = ['SPBase', 'SPBest']
        search_types = [0, 1]
        type = None

        nws = [1, 2, 3, 4, 5]
        base_dir = Global.baseYagoSamplePath + "k_nw_dr\\"
        for search_index in range(len(search_types)):
            for alg_index in range(len(alg_types)):
                if alg_index==0:
                    if search_index==0: type=r'$SPTD$'
                    else:   type=r'$SPTR$'
                else:
                    if search_index==0: type=r'$SPTD^*$'
                    else:   type=r'$SPTR^*$'
                runtimes = []
                for nw in nws:
                    data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_types[alg_index], nwlen=10000000, mds=50000000, t=search_types[search_index], ns=200, r=2, k=5, nw=nw, wf=1000, dr=7))
                    print(data)
                    runtimes.append(data.timeTotal)
                chart.draw_line(runtimes, type)
        chart.show()

    # 画radius的折线图
    @staticmethod
    def draw_radius(type=0, base_y=600, title='TEST', fpath='test.pdf'):
        if type==0:
            ys = [base_y+i*50 for i in range(0, 8)]
        elif type==1:
            ys = [base_y+i*50 for i in range(0, 9)]
        xs=[i for  i in  range(0, 4)]
        x_txts = [1.0, 2.0, 3.0, 5.0]
        chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=r'$\alpha$-radius', yLabel='Runtime (ms)', title=title, fpath=fpath)
        # chart = LineChart(xs, x_txts, xLabel=r'$\alpha$-radius', yLabel='Runtime (ms)')

        radius = [1, 2, 3, 5]
        ks = [1, 3, 5, 8, 10, 15, 20]
        base_dir = 'D:\\nowMask\KnowledgeBase\\sample_result\\yago2s_single_date\\new\\radius_k\\'
        for k in ks:
            runtimes = []
            for r in radius:
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=1000000, mds=50000000, t=type, ns=200, r=r, k=k, nw=5, wf=50, dr=7), time_total_threshold=12000000)
                print(data)
                runtimes.append(data.timeTotal)
            chart.draw_line(runtimes, 'k=' + str(k))
        chart.show()


    # 画radius_len的折线图
    @staticmethod
    def radius_len(type=0, base_y=600, title='TEST', fpath='test.pdf'):
        if type==0:
            ys = [base_y+i*20 for i in range(0, 11)]
            yscale = 'liner'
        elif type==1:
            ys = [base_y+i*40 for i in range(0, 7)]
            yscale = 'liner'
        # x_txts = [1.0, 2.0, 3.0]
        x_txts = (r'$1\times10^5$', r'$1\times10^6$', r'$1\times10^7$')
        # x_txts = (r'$1\times10^5$', r'$1\times10^6$', r'$1\times10^7$', r'$1\times10^8$', r'$1\times10^9$')
        xs=[i for  i in  range(len(x_txts))]
        chart = LineChart(xs, x_txts, ys=ys, yscale=yscale, xLabel=r'l', yLabel='Runtime (ms)', title=title, fpath=fpath, xlabel_rotation=45)
        # chart = LineChart(xs, x_txts, xLabel=r'$\alpha$-radius', yLabel='Runtime (ms)')

        radius = (1, 2, 3, 4)
        radius_txt = (r'$\alpha-radius=1$', r'$\alpha-radius=2$', r'$\alpha-radius=3$', r'$\alpha-radius=4$')
        lens = (100000, 1000000, 10000000)
        # lens = (100000, 1000000, 10000000, 100000000, 1000000000)
        lens_txt = (r'$\mathit{l}=1\times10^5$', r'$\mathit{l}=1\times10^6$', r'$\mathit{l}=1\times10^7$', r'$\mathit{l}=1\times10^8$', r'$\mathit{l}=1\times10^9$')
        base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new\\radius_len\\'
        for radiu_i in range(len(radius)):
            runtimes = []
            for len_i in range(len(lens)):
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=lens[len_i], mds=50000000, t=type, ns=200, r=radius[radiu_i], k=5, nw=5, wf=50, dr=7), time_total_threshold=12000000)
                print(data)
                runtimes.append(data.timeTotal)
            # chart.draw_line(runtimes, lens_txt[len_i])
            chart.draw_line(runtimes, radius_txt[radiu_i])
        chart.show()

    # 绘制不同规模柱状图
    @staticmethod
    def draw_differ_size(line_type=0, title='Graph Vertex Size (in million)', fpath='test.pdf'):
        # testSampleResultFile.t=0.ns=500.r=1.k=10.nw=1
        xs = [1, 2, 3, 4]
        x_txts = [2.0, 4.0, 6.0, 8.0]

        ylim = None

        if line_type==0:
            yLabel='Runtime (ms)'
            ylim = (100, 100000)
        elif line_type==1:
            yLabel = '# of R-tree nodes accessed'
            ylim = (1, 100000)

        chart = LineChart(xs, x_txts, ylim=ylim, xLabel=r'Graph Vertex Size (in million)', yLabel=yLabel, title=title, fpath=fpath)

        alg_types = ['SPBase', 'SPBest']
        ts = [0, 1]
        alg_names = [r'$SPTD$', r'$SPTD^*$', r'$SPTR$', r'$SPTR^*$']
        base_dir = 'D:\\nowMask\\KnowledgeBase\\sample_result\\yago2s_single_date\\new1\\diff_size\\'
        sizes = ('2000000\\', '4000000\\', '6000000\\', 'org\\')

        alg_names_index = 0
        for t in ts:
            for alg_type in alg_types:
                runtimes = []
                for i in range(len(sizes)):
                    data = Data.getData(fp=PathUtility.sample_res_path(base_dir + sizes[i] + '\\', sp=alg_type, nwlen=1000000, mds=50000000, t=t, ns=200, r=2, k=5, nw=5, wf=50, dr=7))
                    print(data)
                    if line_type==0:    runtimes.append(data.timeTotal)
                    elif line_type==1:  runtimes.append(data.numAccessedRTreeNode)
                chart.draw_line(runtimes, label=alg_names[alg_names_index])
                alg_names_index = alg_names_index + 1
        chart.show()


    # 画maxDateSpan=1000，不同的pn长的折线图
    @staticmethod
    def draw_max_pn(label="l", base_y=None, x_rotation=0, fpath='test.pdf'):
        if base_y!=None: ys = [base_y+i*100 for i in range(0, 17)]
        xs=[i for  i in  range(0, 10)]
        lens = [500000000, 100000000, 50000000, 10000000, 5000000, 1000000, 500000, 100000, 10000, 0]
        x_txts = [r'$5\times10^8$', r'$1\times10^8$', r'$5\times10^7$', r'$1\times10^7$',
                  r'$5\times10^6$', r'$1\times10^6$', r'$5\times10^5$', r'$1\times10^5$',
                  r'$1\times10^4$', '0']

        if base_y!=None: chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=label, title=label, yLabel='Runtime (ms)', xlabel_rotation=x_rotation, fpath = fpath)
        else:   chart = LineChart(xs, x_txts, xLabel=label, title=label, yLabel='Runtime (ms)', xlabel_rotation=x_rotation, fpath=fpath)

        types=[0, 1]
        for type in types:
            runtimes = []
            for nwlen in lens:
                data = Data.getData(type, 200, 3, 5, 5, f_nwlen=nwlen, relativePath='yago2s_single_date\\pn_len')
                runtimes.append(data.timeTotal)
            if type==0:
                chart.draw_line(runtimes, r'$SPTD^*$')
            elif type==1:
                chart.draw_line(runtimes, r'$SPTR^*$')
        chart.ax.grid('on')
        chart.show()

    # 画pn=10000000，不同maxDateSpan的折线图
    @staticmethod
    def draw_max_date(nwlen=1000000, label='MAX_DATE_DIFFERENCE', base_y=None, x_rotation=0, fpath='test.pdf'):
        if base_y!=None: ys = [base_y+i*50 for i in range(0, 11)]
        xs=[i for  i in  range(0, 9)]
        x_txts = [r'$2.1\times10^8$', r'$5\times10^3$', r'$2.5\times10^3$', r'$1\times10^3$',
                  r'$7.5\times10^2$', r'$5\times10^2$', r'$2.5\times10^2$', r'$1\times10^2$',
                  r'$1$']
        maxDates = [210000000, 5000, 2500, 1000, 750, 500, 250, 100, 1]
        if base_y!=None: chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=label, yLabel='Runtime (ms)', title=label, xlabel_rotation=x_rotation, fpath = fpath)
        else: chart = LineChart(xs, x_txts, xLabel=label, yLabel='Runtime (ms)', title=label, xlabel_rotation=x_rotation, fpath = fpath)

        chart.ax.grid('on')

        types=[0, 1]
        for type in types:
            runtimes = []
            for maxDate in maxDates:
                data = Data.getData(type, 200, 3, 5, 5, relativePath='nw=10\\MAX_DATE', f_nwlen=nwlen, f_mds=maxDate)
                runtimes.append(data.timeTotal)
            if type==0:
                chart.draw_line(runtimes, r'$SPTD^*$')
            elif type==1:
                chart.draw_line(runtimes, r'$SPTR^*$')
        chart.show()

    @staticmethod
    def draw_word_frequency(title=r'$\tau$', search_type=0, base_y=None, rotation=0, fpath='test.pdf'):
        if base_y!=None:
            if search_type==0:
                ys = [base_y+i*2000 for i in range(0, 7)]
            elif search_type==1:
                ys = [base_y+i*2000 for i in range(0, 7)]
        xs=[i for  i in  range(0, 8)]
        x_txts = [r'$5\times10^1$', r'$1\times10^2$', r'$2.5\times10^2$',
                     r'$5\times10^2$', r'$1\times10^3$',
                     r'$1\times10^4$',r'$1\times10^5$',
                     r'$1\times10^6$'
                 ]
        if base_y!=None: chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=title, yLabel='Runtime (ms)', title=title, xlabel_rotation=rotation, fpath = fpath)
        else: chart = LineChart(xs, x_txts, xLabel=title, yLabel='Runtime (ms)', title=title, xlabel_rotation=rotation, fpath = fpath)

        # chart.ax.grid('on')

        wfs = [50, 100, 250, 500, 1000, 10000, 100000, 1000000]

        base_dir = Global.baseYagoSamplePath + 'word_frequency\\'
        search_name = None
        if search_type==0:
            search_name = r'$SPTD^*$'
        else:
            search_name = r'$SPTR^*$'

        search_types = (0, 1)
        search_names = (r'$SPTD^*$', r'$SPTR^*$')

        for search_type_index in range(len(search_types)):
            runtimes = []
            for wf in wfs:
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp='SPBest', nwlen=10000000, mds=50000000, t=search_types[search_type_index], ns=200, r=2, k=5, nw=3, wf=wf, dr=7), time_total_threshold=12000000)
                runtimes.append(data.timeTotal)
            chart.draw_line(runtimes, search_names[search_type_index])
        chart.show()

    @staticmethod
    def draw_word_frequency_size(title='WORD_FREQUENCY_SIZE', base_y=None, rotation=0, fpath='test.pdf'):
        if base_y!=None: ys = [base_y+i*2000 for i in range(0, 21)]
        xs=[i for  i in  range(0, 10)]
        x_txts = [0, r'$5\times10^1$', r'$1\times10^2$',
                     r'$5\times10^2$', r'$1\times10^3$',
                     r'$5\times10^3$', r'$1\times10^4$',
                     r'$5\times10^4$', r'$1\times10^5$',
                     r'$1\times10^6$'
                 ]
        if base_y!=None: chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=title, yLabel='Size (MB)', title=title, xlabel_rotation=rotation, fpath = fpath)
        else: chart = LineChart(xs, x_txts, xLabel=title, yLabel='Size (MB)', title=title, xlabel_rotation=rotation, fpath = fpath)

        chart.ax.grid('on')

        path = 'D:\\nowMask\KnowledgeBase\\sample_result\\yago2s_single_date\\recordPid2WidSize.txt'
        sizes = []
        with open(path) as f:
            f.readline()
            while True:
                line = f.readline()
                if line=='':    break
                sizes.append(float(line.split(': ')[1][:-1]))
        print(len(sizes))
        del(sizes[-1])
        sizes.append(0)
        chart.draw_line(sizes, r'$SPTD^*$')
        chart.show()

     # 画不同date range折线图
    @staticmethod
    def draw_date_range(label=r'$|q.\delta|$', base_y=None, x_rotation=0, fpath='test.pdf'):
        if base_y!=None: ys = [base_y+i*100 for i in range(0, 17)]

        # x_txts = [r'$5\times10^8$', r'$1\times10^8$', r'$5\times10^7$', r'$1\times10^7$',
        #           r'$5\times10^6$', r'$1\times10^6$', r'$5\times10^5$', r'$1\times10^5$',
        #           r'$1\times10^4$', '0']
        # x_txts = [0,3,7,15,30,50,100,150,300,600]
        # drs = [0,3,7,15,30,50,100,150,300,600]
        drs = [0,3,7,15,30,50,100,150]
        x_txts = [1,7,14,30,60,100,200,300]
        xs=[i for  i in  range(len(drs))]

        if base_y!=None: chart = LineChart(xs, x_txts, ys=ys, yscale='liner', xLabel=label, title=label, yLabel='Runtime (ms)', xlabel_rotation=x_rotation, fpath = fpath)
        else:   chart = LineChart(xs, x_txts, xLabel=label, title=label, ylim=(1000, 100000), yLabel='Runtime (ms)', xlabel_rotation=x_rotation, fpath=fpath)

        base_dir = Global.baseYagoSamplePath + "k_nw_dr\\"
        alg_types=['SPBase', 'SPBest']
        for alg_type in alg_types:
            runtimes = []
            for dr in drs:
                data = Data.getData(fp=PathUtility.sample_res_path(base_dir, sp=alg_type, nwlen=10000000, mds=50000000, t=1, ns=200, r=2, k=5, nw=3, wf=1000, dr=dr))
                # print(data)
                runtimes.append(data.timeTotal)
            print(runtimes)
            if alg_type=='SPBase':
                chart.draw_line(runtimes, r'$SPTR$')
            elif alg_type=='SPBest':
                chart.draw_line(runtimes, r'$SPTR^*$')
        chart.show()

    # 画节点度的分布图
    @staticmethod
    def draw_degree_distribution():
        fpath = PathUtility.output_path + "degreeYagoVB.txt"
        org_data = []
        max_degree = 0
        with open(fpath) as fp:
            line = ','
            while True:
                line = fp.readline()
                if line=='': break
                if line=="\n": continue
                line = line[:-1]
                # print(line)
                ints = line.split(': ')
                degree = int(ints[1])
                if max_degree<degree:   max_degree = degree
                org_data.append(degree)
        # print(org_data)
        print(max_degree)
        degree_nums = {}
        for i in range(max_degree+1):
            degree_nums[i] = 0

        for degree in org_data:
            if degree in degree_nums:
                degree_nums[degree] = degree_nums[degree] + 1
            else:
                degree_nums[degree] = 1
        # print(degree_nums)
        nums = []
        degrees = sorted(degree_nums.keys())
        print(degrees)
        for dg in degrees:
            nums.append(degree_nums[dg])
        # plt.xlim([0, 1400])
        figure = plt.figure()
        ax1 = figure.add_subplot(221)
        ax1.plot(degrees, nums)
        # ax1.scatter(degrees, nums)

        ax2 = figure.add_subplot(222)
        ax2.set_ylim([0, 100])
        ax2.plot(degrees, nums)

        ax3 = figure.add_subplot(223)
        ax3.set_ylim([0, 10])
        ax3.plot(degrees, nums)

        ax4 = figure.add_subplot(224)
        ax4.set_ylim([0, 5])
        ax4.plot(degrees, nums)

        plt.show()



    @staticmethod
    def sleep():
        plt.pause(1200)

######## 画WORD_FREQUENCY折线图 ##############
# LineChart.draw_word_frequency(base_y=1000, search_type=0, rotation=45, fpath=PathUtility.figure_path() + 'WordFrequency_RuntimeYagoVB_Date.pdf')
# LineChart.draw_word_frequency(base_y=400, search_type=1, rotation=45, fpath=PathUtility.figure_path() + 'WordFrequency_RuntimeYagoVB_SPTRStar1.pdf')


######### 画radius_len柱状图 ################
# Bar.draw_radius_len(0, 0, ftype='alpha_len_SPTD*', fpath=PathUtility.figure_path() + 'AlphaLenBar_RuntimeYagoVB_SPTDStar.pdf')
# Bar.draw_radius_len(1, 0, ftype='alpha_len_SPTR*', fpath=PathUtility.figure_path() + 'AlphaLenBar_RuntimeYagoVB_SPTRStar.pdf')

######### 画top-k柱状图 #################
# Bar.draw_topK()
# Bar.draw_topK(1)
# Bar.draw_topK(0, 1)
# Bar.draw_topK(1, 1)
# Bar.draw_topK(0, 2)
# Bar.draw_topK(1, 2)
# 折线图
LineChart.draw_k(0, fpath=PathUtility.figure_path() + 'topK_RuntimeYagoVB_Date.pdf')
LineChart.draw_k(1, base_y=0, fpath=PathUtility.figure_path() + 'topK_TQTSPYagoVB_Date.pdf')
LineChart.draw_k(2, fpath=PathUtility.figure_path() + 'topK_RTreeNodeYagoVB_Date.pdf')


######### 画不同数keywords柱状图 #########
# Bar.draw_n_words()
# Bar.draw_n_words(1)
# 折线图
# LineChart.draw_nw(fpath=PathUtility.figure_path() + 'WordNum_RuntimeYagoVB_Date.pdf')

######## 画不同的时间差对查询情况影响  #########
# LineChart.draw_date_range(fpath=PathUtility.figure_path() + 'DateRange_RuntimeYagoVB_SPTRStar.pdf')

######### 画不同规模子图的柱状图  #########
# Bar.draw_differ_size(0, 0)
# Bar.draw_differ_size(0, 2)
# Bar.draw_differ_size(1, 0)
# Bar.draw_differ_size(1, 2)
# 画折线图
# LineChart.draw_differ_size(0, fpath=PathUtility.figure_path() + 'DiffSize_RuntimeYagoVB_Date.pdf')
# LineChart.draw_differ_size(1, fpath=PathUtility.figure_path() + 'DiffSize_RTreeNodeYagoVB_Date.pdf')













######### 画radius_k折线图 ################
# LineChart.draw_radius(0, 750, title="radius_k SPTD*", fpath=PathUtility.figure_path() + 'Radius_RuntimeYago_SPTDStar.pdf')
# LineChart.draw_radius(1, 400, title="radius_k SPTR*", fpath=PathUtility.figure_path() + 'Radius_RuntimeYago_SPTRStar.pdf')

######### 画radius_len折线图 ################
# LineChart.radius_len(0, 790, title="radius_len SPTD*", fpath=PathUtility.figure_path() + 'RadiusLenLine_RuntimeYago_SPTDStar.pdf')
# LineChart.radius_len(1, 400, title="radius_len SPTR*", fpath=PathUtility.figure_path() + 'RadiusLenLine_RuntimeYago_SPTRStar.pdf')

######### 画MAX_PN折线图 ################
# LineChart.draw_max_pn(label='l', x_rotation=45, fpath=PathUtility.figure_path() + 'MaxPNYagoVB.pdf')

######## 画MAX_DIFFERENCE折线图 ##############
# LineChart.draw_max_date(base_y=400, x_rotation=45, fpath=PathUtility.figure_path() + 'MaxDateDifferenceYagoVB.pdf')

######## 画不同WORD_FREQUENCY_SIZE折线图 #####
# LineChart.draw_word_frequency_size(base_y=0, rotation=45, fpath=PathUtility.figure_path() + 'WordFrequencySizeYagoVB.pdf')

######## 画节点度的分布图  #########
# LineChart.draw_degree_distribution()

Bar.sleep()

###########################  Bar 测试代码 ###############################################
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

########################### LineChart测试代码 ######################################################
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
