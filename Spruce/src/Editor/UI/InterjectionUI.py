# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Interjection.ui'
#
# Created: Sun Jul 28 01:23:04 2013
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

class Ui_Form(object):
    def setupUi(self, Form):
        Form.setObjectName(_fromUtf8("Form"))
        Form.resize(677, 555)
        self.horizontalLayout = QtGui.QHBoxLayout(Form)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.verticalFrame = QtGui.QFrame(Form)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(200)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.verticalFrame.sizePolicy().hasHeightForWidth())
        self.verticalFrame.setSizePolicy(sizePolicy)
        self.verticalFrame.setMaximumSize(QtCore.QSize(200, 16777215))
        self.verticalFrame.setObjectName(_fromUtf8("verticalFrame"))
        self.verticalLayout_3 = QtGui.QVBoxLayout(self.verticalFrame)
        self.verticalLayout_3.setMargin(0)
        self.verticalLayout_3.setObjectName(_fromUtf8("verticalLayout_3"))
        self.label_3 = QtGui.QLabel(self.verticalFrame)
        self.label_3.setMaximumSize(QtCore.QSize(200, 16777215))
        self.label_3.setObjectName(_fromUtf8("label_3"))
        self.verticalLayout_3.addWidget(self.label_3)
        self.lWords = QtGui.QListWidget(self.verticalFrame)
        self.lWords.setMaximumSize(QtCore.QSize(200, 16777215))
        self.lWords.setObjectName(_fromUtf8("lWords"))
        self.verticalLayout_3.addWidget(self.lWords)
        self.bRemove = QtGui.QPushButton(self.verticalFrame)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.bRemove.sizePolicy().hasHeightForWidth())
        self.bRemove.setSizePolicy(sizePolicy)
        self.bRemove.setObjectName(_fromUtf8("bRemove"))
        self.verticalLayout_3.addWidget(self.bRemove)
        self.horizontalLayout.addWidget(self.verticalFrame)
        self.line = QtGui.QFrame(Form)
        self.line.setFrameShape(QtGui.QFrame.VLine)
        self.line.setFrameShadow(QtGui.QFrame.Sunken)
        self.line.setObjectName(_fromUtf8("line"))
        self.horizontalLayout.addWidget(self.line)
        self.gridLayout = QtGui.QGridLayout()
        self.gridLayout.setObjectName(_fromUtf8("gridLayout"))
        self.singularLabel_3 = QtGui.QLabel(Form)
        self.singularLabel_3.setObjectName(_fromUtf8("singularLabel_3"))
        self.gridLayout.addWidget(self.singularLabel_3, 0, 0, 1, 1)
        self.tInterj = QtGui.QLineEdit(Form)
        self.tInterj.setObjectName(_fromUtf8("tInterj"))
        self.gridLayout.addWidget(self.tInterj, 0, 1, 1, 1)
        self.singularSyllablesLabel_3 = QtGui.QLabel(Form)
        self.singularSyllablesLabel_3.setObjectName(_fromUtf8("singularSyllablesLabel_3"))
        self.gridLayout.addWidget(self.singularSyllablesLabel_3, 1, 0, 1, 1)
        self.interjSyl = QtGui.QSpinBox(Form)
        self.interjSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.interjSyl.setObjectName(_fromUtf8("interjSyl"))
        self.gridLayout.addWidget(self.interjSyl, 1, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(20, 2000, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout.addItem(spacerItem, 2, 1, 1, 1)
        self.horizontalLayout_5 = QtGui.QHBoxLayout()
        self.horizontalLayout_5.setObjectName(_fromUtf8("horizontalLayout_5"))
        spacerItem1 = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_5.addItem(spacerItem1)
        self.bAdd = QtGui.QPushButton(Form)
        self.bAdd.setObjectName(_fromUtf8("bAdd"))
        self.horizontalLayout_5.addWidget(self.bAdd)
        self.gridLayout.addLayout(self.horizontalLayout_5, 3, 1, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(Form)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        Form.setWindowTitle(_translate("Form", "Interjections", None))
        self.label_3.setText(_translate("Form", "Interjections;", None))
        self.bRemove.setText(_translate("Form", "Remove Entry", None))
        self.singularLabel_3.setText(_translate("Form", "Interjection:", None))
        self.singularSyllablesLabel_3.setText(_translate("Form", "Syllables:", None))
        self.bAdd.setText(_translate("Form", "Add/Update Entry", None))

