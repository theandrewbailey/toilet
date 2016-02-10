# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Spruce.ui'
#
# Created: Sun Jul 28 01:23:05 2013
#      by: PyQt4 UI code generator 4.10
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    def _fromUtf8(s):
        return s

try:
    _encoding = QtGui.QApplication.UnicodeUTF8
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig, _encoding)
except AttributeError:
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig)

class Ui_Spruce(object):
    def setupUi(self, Spruce):
        Spruce.setObjectName(_fromUtf8("Spruce"))
        Spruce.resize(600, 600)
        self.centralwidget = QtGui.QWidget(Spruce)
        self.centralwidget.setObjectName(_fromUtf8("centralwidget"))
        self.verticalLayout_7 = QtGui.QVBoxLayout(self.centralwidget)
        self.verticalLayout_7.setObjectName(_fromUtf8("verticalLayout_7"))
        self.tabWidget = QtGui.QTabWidget(self.centralwidget)
        self.tabWidget.setObjectName(_fromUtf8("tabWidget"))
        self.verticalLayout_7.addWidget(self.tabWidget)
        self.horizontalLayout_2 = QtGui.QHBoxLayout()
        self.horizontalLayout_2.setObjectName(_fromUtf8("horizontalLayout_2"))
        self.bGen = QtGui.QPushButton(self.centralwidget)
        self.bGen.setObjectName(_fromUtf8("bGen"))
        self.horizontalLayout_2.addWidget(self.bGen)
        self.bMass = QtGui.QPushButton(self.centralwidget)
        self.bMass.setObjectName(_fromUtf8("bMass"))
        self.horizontalLayout_2.addWidget(self.bMass)
        spacerItem = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_2.addItem(spacerItem)
        self.bReload = QtGui.QPushButton(self.centralwidget)
        self.bReload.setObjectName(_fromUtf8("bReload"))
        self.horizontalLayout_2.addWidget(self.bReload)
        self.bSave = QtGui.QPushButton(self.centralwidget)
        self.bSave.setObjectName(_fromUtf8("bSave"))
        self.horizontalLayout_2.addWidget(self.bSave)
        self.verticalLayout_7.addLayout(self.horizontalLayout_2)
        Spruce.setCentralWidget(self.centralwidget)

        self.retranslateUi(Spruce)
        self.tabWidget.setCurrentIndex(-1)
        QtCore.QMetaObject.connectSlotsByName(Spruce)

    def retranslateUi(self, Spruce):
        Spruce.setWindowTitle(_translate("Spruce", "Spruce Dictionary Editor", None))
        self.bGen.setText(_translate("Spruce", "Generate", None))
        self.bMass.setText(_translate("Spruce", "Mass Entry", None))
        self.bReload.setText(_translate("Spruce", "Reload", None))
        self.bSave.setText(_translate("Spruce", "Save", None))

