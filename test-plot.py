import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import json

# Cambiar el backend a TkAgg para interactividad
import matplotlib
matplotlib.use('TkAgg')

# Leer el archivo JSON
with open('tree-matrix-gen/run/block_matrix.json', 'r') as file:
    data = json.load(file)

coordinates = []
values = []

for key, value in data.items():
    x, y, z = map(int, key.split(', '))
    coordinates.append((x, y, z))
    values.append(value)

x_coords = [coord[2] for coord in coordinates]
y_coords = [coord[0] for coord in coordinates]
z_coords = [coord[1] for coord in coordinates]

colors = []
for value in values:
    if value == 0:
        colors.append('black')
    elif value == 1:
        colors.append('pink')
    else:
        colors.append('gray')

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

scatter = ax.scatter(x_coords, y_coords, z_coords, c=colors)

ax.set_xlabel('X')
ax.set_ylabel('Y')
ax.set_zlabel('Z')

plt.show()