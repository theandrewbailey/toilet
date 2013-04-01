# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Adjective.ui'
#
# Created: Sat Feb  5 14:25:12 2011
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

class Ui_Form(object):
    def setupUi(self, Form):
        Form.setObjectName("Form")
        Form.resize(467, 322)
        self.horizontalLayout = QtGui.QHBoxLayout(Form)
        self.horizontalLayout.setObjectName("horizontalLayout")
        self.verticalFrame = QtGui.QFrame(Form)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(200)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.verticalFrame.sizePolicy().hasHeightForWidth())
        self.verticalFrame.setSizePolicy(sizePolicy)
        self.verticalFrame.setMaximumSize(QtCore.QSize(200, 16777215))
        self.verticalFrame.setObjectName("verticalFrame")
        self.verticalLayout_3 = QtGui.QVBoxLayout(self.verticalFrame)
        self.verticalLayout_3.setMargin(0)
        self.verticalLayout_3.setObjectName("verticalLayout_3")
        self.label_3 = QtGui.QLabel(self.verticalFrame)
        self.label_3.setMaximumSize(QtCore.QSize(200, 16777215))
        self.label_3.setObjectName("label_3")
        self.verticalLayout_3.addWidget(self.label_3)
        self.lWords = QtGui.QListWidget(self.verticalFrame)
        self.lWords.setMaximumSize(QtCore.QSize(200, 16777215))
        self.lWords.setObjectName("lWords")
        self.verticalLayout_3.addWidget(self.lWords)
        self.bRemove = QtGui.QPushButton(self.verticalFrame)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.bRemove.sizePolicy().hasHeightForWidth())
        self.bRemove.setSizePolicy(sizePolicy)
        self.bRemove.setObjectName("bRemove")
        self.verticalLayout_3.addWidget(self.bRemove)
        self.horizontalLayout.addWidget(self.verticalFrame)
        self.line = QtGui.QFrame(Form)
        self.line.setFrameShape(QtGui.QFrame.VLine)
        self.line.setFrameShadow(QtGui.QFrame.Sunken)
        self.line.setObjectName("line")
        self.horizontalLayout.addWidget(self.line)
        self.gridLayout = QtGui.QGridLayout()
        self.gridLayout.setObjectName("gridLayout")
        self.singularLabel_3 = QtGui.QLabel(Form)
        self.singularLabel_3.setObjectName("singularLabel_3")
        self.gridLayout.addWidget(self.singularLabel_3, 0, 0, 1, 1)
        self.tAdj = QtGui.QLineEdit(Form)
        self.tAdj.setObjectName("tAdj")
        self.gridLayout.addWidget(self.tAdj, 0, 1, 1, 1)
        self.singularSyllablesLabel_3 = QtGui.QLabel(Form)
        self.singularSyllablesLabel_3.setObjectName("singularSyllablesLabel_3")
        self.gridLayout.addWidget(self.singularSyllablesLabel_3, 1, 0, 1, 1)
        self.adjSyl = QtGui.QSpinBox(Form)
        self.adjSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.adjSyl.setObjectName("adjSyl")
        self.gridLayout.addWidget(self.adjSyl, 1, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(20, 2000, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout.addItem(spacerItem, 2, 1, 1, 1)
        self.horizontalLayout_5 = QtGui.QHBoxLayout()
        self.horizontalLayout_5.setObjectName("horizontalLayout_5")
        spacerItem1 = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_5.addItem(spacerItem1)
        self.bAdd = QtGui.QPushButton(Form)
        self.bAdd.setObjectName("bAdd")
        self.horizontalLayout_5.addWidget(self.bAdd)
        self.gridLayout.addLayout(self.horizontalLayout_5, 3, 1, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(Form)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        Form.setWindowTitle(QtGui.QApplication.translate("Form", "Adjectives", None, QtGui.QApplication.UnicodeUTF8))
        self.label_3.setText(QtGui.QApplication.translate("Form", "Adjectives:", None, QtGui.QApplication.UnicodeUTF8))
        self.bRemove.setText(QtGui.QApplication.translate("Form", "Remove Entry", None, QtGui.QApplication.UnicodeUTF8))
        self.singularLabel_3.setText(QtGui.QApplication.translate("Form", "Adjective:", None, QtGui.QApplication.UnicodeUTF8))
        self.singularSyllablesLabel_3.setText(QtGui.QApplication.translate("Form", "Syllables:", None, QtGui.QApplication.UnicodeUTF8))
        self.bAdd.setText(QtGui.QApplication.translate("Form", "Add/Update Entry", None, QtGui.QApplication.UnicodeUTF8))

