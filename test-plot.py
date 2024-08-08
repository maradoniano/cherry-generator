import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import json
import numpy as np
from collections import defaultdict

with open('tree-matrix-gen/run/block_matrix.json', 'r') as file:
    data = json.load(file)

coordinates = defaultdict(list)

for matrix in data:
    for key, value in matrix.items():
        if value == 0:
            x, y, z = map(int, key.split(', '))
            coordinates[(x, y, z)].append(value)

stats = {}
for coord, values in coordinates.items():
    values_array = np.array(values)
    stats[coord] = {
        'mean': np.mean(values_array),
        'std': np.std(values_array),
        'max': np.max(values_array),
        'min': np.min(values_array),
        'count': len(values_array)
    }

counts = [stat['count'] for stat in stats.values()]
min_count = min(counts)
max_count = max(counts)
normalized_counts = [(0.1 + (count - min_count) / (max_count - min_count) * 0.9) for count in counts]

x_coords = [coord[2] for coord in stats.keys()]
y_coords = [coord[0] for coord in stats.keys()]
z_coords = [coord[1] for coord in stats.keys()]
means = [stat['mean'] for stat in stats.values()]

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

for x, y, z, mean, alpha in zip(x_coords, y_coords, z_coords, means, normalized_counts):
    ax.scatter(x, y, z, c=[mean], cmap='plasma', s=50, alpha=alpha)

sc = ax.scatter(x_coords, y_coords, z_coords, c=means, cmap='plasma', s=50)
cbar = plt.colorbar(sc)
cbar.set_label('Mean Value')

ax.set_xlabel('Z')
ax.set_ylabel('X')
ax.set_zlabel('Y')

plt.show()
