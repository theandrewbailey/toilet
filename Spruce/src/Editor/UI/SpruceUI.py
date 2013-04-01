# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Spruce.ui'
#
# Created: Sat Feb  5 14:25:12 2011
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

class Ui_Spruce(object):
    def setupUi(self, Spruce):
        Spruce.setObjectName("Spruce")
        Spruce.resize(600, 600)
        self.centralwidget = QtGui.QWidget(Spruce)
        self.centralwidget.setObjectName("centralwidget")
        self.verticalLayout_7 = QtGui.QVBoxLayout(self.centralwidget)
        self.verticalLayout_7.setObjectName("verticalLayout_7")
        self.tabWidget = QtGui.QTabWidget(self.centralwidget)
        self.tabWidget.setObjectName("tabWidget")
        self.verticalLayout_7.addWidget(self.tabWidget)
        self.horizontalLayout_2 = QtGui.QHBoxLayout()
        self.horizontalLayout_2.setObjectName("horizontalLayout_2")
        self.bGen = QtGui.QPushButton(self.centralwidget)
        self.bGen.setObjectName("bGen")
        self.horizontalLayout_2.addWidget(self.bGen)
        self.bMass = QtGui.QPushButton(self.centralwidget)
        self.bMass.setObjectName("bMass")
        self.horizontalLayout_2.addWidget(self.bMass)
        spacerItem = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_2.addItem(spacerItem)
        self.bReload = QtGui.QPushButton(self.centralwidget)
        self.bReload.setObjectName("bReload")
        self.horizontalLayout_2.addWidget(self.bReload)
        self.bSave = QtGui.QPushButton(self.centralwidget)
        self.bSave.setObjectName("bSave")
        self.horizontalLayout_2.addWidget(self.bSave)
        self.verticalLayout_7.addLayout(self.horizontalLayout_2)
        Spruce.setCentralWidget(self.centralwidget)

        self.retranslateUi(Spruce)
        self.tabWidget.setCurrentIndex(-1)
        QtCore.QMetaObject.connectSlotsByName(Spruce)

    def retranslateUi(self, Spruce):
        Spruce.setWindowTitle(QtGui.QApplication.translate("Spruce", "Spruce Dictionary Editor", None, QtGui.QApplication.UnicodeUTF8))
        self.bGen.setText(QtGui.QApplication.translate("Spruce", "Generate", None, QtGui.QApplication.UnicodeUTF8))
        self.bMass.setText(QtGui.QApplication.translate("Spruce", "Mass Entry", None, QtGui.QApplication.UnicodeUTF8))
        self.bReload.setText(QtGui.QApplication.translate("Spruce", "Reload", None, QtGui.QApplication.UnicodeUTF8))
        self.bSave.setText(QtGui.QApplication.translate("Spruce", "Save", None, QtGui.QApplication.UnicodeUTF8))

