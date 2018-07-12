import matplotlib.pyplot as plt
import datetime
import numpy as np

x = [datetime.datetime(2010, 12, 1, 0, 0),
     datetime.datetime(2011, 1, 1, 0, 0),
     datetime.datetime(2011, 5, 1, 1, 0)]
y = [4, 9, 2]

fig, ax = plt.subplots()
ax.bar(x, y, width = 20, align='center')

counts = np.random.randint(0, 25, len(ax.get_xticks()))

for i, xpos in enumerate(ax.get_xticks()):
    ax.text(xpos,-1, "Below tick\nlabel "+str(i),
            size = 6, ha = 'center')

    ax.text(xpos, -1.25, "Count: "+str(counts[i]),
            size = 6, ha = 'center')

plt.show()
