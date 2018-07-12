import numpy as np
import matplotlib.pyplot as plt
import copy
from matplotlib.transforms import Bbox

fig = plt.figure()
ax1 = fig.add_subplot(111)

xs = [1, 2, 3]
box = Bbox([[0,0], [1, 1]])

# ax1.fill([0, 1, 1, 0], [0, 0, 1, 1], hatch='x')
ax1.fill([0, 1, 1, 0], [0, 0, 1, 1], hatch=None)
ax1.tick_params(direction='in')
xaxis = ax1.get_xaxis()
xtick_labels = xaxis.get_ticklabels()

# print(xtick_labels)
ax1.text(0.5, -0.1, 'wo', fontsize=12, ha='center')
# print(xtick_labels[0].get_position()[0])

# ax1.set_xlim(1, 10)
xtick_lines = ax1.get_xticklines()

# fig.canvas.draw()
# for tl in xtick_lines:
#     # print(tl)
#     tl.set_visible(False)
xtick_labels = ax1.get_xlabel()

line = xtick_lines[0]
line.set_visible(True)
print(line)
ax1.plot(lines=[line])
ax1.set_xticks(xs)
# for tl in xtick_labels:
#     tl.set_visible(False)
# xaxis.set_ticklabels(xtick_labels)

plt.show()
