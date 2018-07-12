import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()

ax1 = fig.add_subplot(111)

x=np.array([1.0,3.0,5.0,8.0,10.0, 15.0, 20.0])
n_bins = np.array([10, 20, 30])
b = np.array([1, 2, 4, 5, 6, 4, 34, 34, 3, 3,4])

a = np.array([22,87,5,43,56,73,55,54,11,20,51,5,79,31,27])
# plt.hist(a, bins =  [0,20,40,60,80,100])

colors = ['red', 'tan', 'lime']
# ax1.hist(x, n_bins, density=True, histtype='bar', color=colors, label=colors)
# ax1.hist(b, bins=[0,20,40,60,80,100])
numWids = np.array([1, 3, 5, 8, 10, 15, 20])

xs = np.array([0, 1, 2, 3, 4, 5, 6, 7, 8])
bar_x = np.array([1, 2, 3, 4, 5, 6, 7])
bar_height = bar_x * 50
ax1.bar(bar_x-0.2, bar_height, width=0.2, tick_label='ksp')
ax1.bar(bar_x, bar_height, width=0.2, tick_label='bsp', align='center')
ax1.bar(bar_x+0.2, bar_height, width=0.2, tick_label='sp', align='center')
ax1.set_ylim([1, 10000])
ax1.set_yscale('log')
ax1.set_ylabel('Runtime(ms)')
# ax1.xaxis.tick_top()
# ax1.set_xticks(xs)
ax1.legend()

ax2 = ax1.twinx()
ax2.set_ylim([1, 10000])
ax2.set_yscale('log')

# ax3 = ax1.twiny()
# ax3.xaxis.tick_top()
# ax3.set_xticks(xs)
# ax3.bar(bar_x, bar_height, width=0.2)

plt.legend()
plt.tight_layout()

plt.show()
