import glob
import os
import numpy as np
import matplotlib.pyplot as plt

# 读取pid文件


def readPidFile(pidFile):
    pid = np.loadtxt(pidFile)
    # 每四行分为四组，四个机器人
    pid = pid.reshape(-1, 4, 3)
    return pid


# 画图,仅显示前n个点q
def drawPid(pid, n=7000):
    plt.figure()
    # 每一列分别绘图，总行数为时间，各列分别为errorPos，errorAngle
    # 画图时，x轴为时间，y轴为各列的值
    # plt.plot(pid[:n, 1, 2],label='errorPos'+str(1))
    # 散点图
    # # 根据n生成x轴
    # x = np.arange(0, n)
    # plt.scatter(x,pid[:n, 1, 2],label='errorPos'+str(1),s=1)
    for i in range(1):
        # plt.plot(pid[:n, i, 0],label='lineVelecityAngle'+str(i))
        # plt.plot(pid[:n, i, 1],label='Heading'+str(i))
        plt.plot(pid[:n, i, 2], label='errorAngle'+str(i))
        # plt.plot(pid[:n, i, 0],label='lineVelecity'+str(i))
        # plt.plot(pid[:n, i, 1],label='AngularVelocity'+str(i))
        # plt.plot(pid[:n, i, 0],label='errorX'+str(i))
        # plt.plot(pid[:n, i, 1],label='errorY'+str(i))
        # plt.plot(pid[:n, i, 2],label='errorPos'+str(i))
        # plt.plot(pid[:n, i, 3],label='errorAngle'+str(i))
    # plt.plot(pid[:n, 0, 0],label='errorX')
    # plt.plot(pid[:n, 0, 1],label='errorY')
    plt.title('pid')
    plt.xlabel('time')
    plt.legend()
    plt.show()


if __name__ == '__main__':
    # pidFile = 'predict.txt'
    # pidFile = 'predictjifen.txt'
    # pidFile = 'speed.txt'
    pidFile = '../log/speedAngle.txt'
    # 获取文件夹下最新的txt文件
    # pidFile = max(glob.iglob('*.txt'), key=os.path.getctime)
    pid = readPidFile(pidFile)
    # pidjifen = readPidFile('predictjifen.txt')
    # # 相减
    # pid = pid - pidjifen
    # plt.figure()
    # for i in range(1):
    #     # plt.plot(pid[:7000, i, 0],label='errorPos'+str(i))
    #     plt.plot(pidjifen[:7000, i, 0],label='errorPosjifen'+str(i))

    # plt.title('pid')
    # plt.xlabel('time')
    # plt.legend()
    # plt.show()

    drawPid(pid)
