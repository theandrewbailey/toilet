# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Conjunction.ui'
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
        Form.resize(614, 437)
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
        self.tConj = QtGui.QLineEdit(Form)
        self.tConj.setObjectName(_fromUtf8("tConj"))
        self.gridLayout.addWidget(self.tConj, 0, 1, 1, 1)
        self.singularSyllablesLabel_3 = QtGui.QLabel(Form)
        self.singularSyllablesLabel_3.setObjectName(_fromUtf8("singularSyllablesLabel_3"))
        self.gridLayout.addWidget(self.singularSyllablesLabel_3, 1, 0, 1, 1)
        self.conjSyl = QtGui.QSpinBox(Form)
        self.conjSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.conjSyl.setObjectName(_fromUtf8("conjSyl"))
        self.gridLayout.addWidget(self.conjSyl, 1, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem, 2, 1, 1, 1)
        self.relativeLabel = QtGui.QLabel(Form)
        self.relativeLabel.setObjectName(_fromUtf8("relativeLabel"))
        self.gridLayout.addWidget(self.relativeLabel, 3, 0, 1, 1)
        self.tRel = QtGui.QLineEdit(Form)
        self.tRel.setObjectName(_fromUtf8("tRel"))
        self.gridLayout.addWidget(self.tRel, 3, 1, 1, 1)
        self.relativeSyllablesLabel = QtGui.QLabel(Form)
        self.relativeSyllablesLabel.setObjectName(_fromUtf8("relativeSyllablesLabel"))
        self.gridLayout.addWidget(self.relativeSyllablesLabel, 4, 0, 1, 1)
        self.relSyl = QtGui.QSpinBox(Form)
        self.relSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.relSyl.setObjectName(_fromUtf8("relSyl"))
        self.gridLayout.addWidget(self.relSyl, 4, 1, 1, 1)
        spacerItem1 = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem1, 5, 1, 1, 1)
        self.rCoord = QtGui.QRadioButton(Form)
        self.rCoord.setObjectName(_fromUtf8("rCoord"))
        self.gridLayout.addWidget(self.rCoord, 6, 1, 1, 1)
        self.rSub = QtGui.QRadioButton(Form)
        self.rSub.setObjectName(_fromUtf8("rSub"))
        self.gridLayout.addWidget(self.rSub, 7, 1, 1, 1)
        spacerItem2 = QtGui.QSpacerItem(20, 2000, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout.addItem(spacerItem2, 8, 1, 1, 1)
        self.horizontalLayout_5 = QtGui.QHBoxLayout()
        self.horizontalLayout_5.setObjectName(_fromUtf8("horizontalLayout_5"))
        spacerItem3 = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_5.addItem(spacerItem3)
        self.bAdd = QtGui.QPushButton(Form)
        self.bAdd.setObjectName(_fromUtf8("bAdd"))
        self.horizontalLayout_5.addWidget(self.bAdd)
        self.gridLayout.addLayout(self.horizontalLayout_5, 9, 1, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(Form)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        Form.setWindowTitle(_translate("Form", "Conjunctions", None))
        self.label_3.setText(_translate("Form", "Conjunctions:", None))
        self.bRemove.setText(_translate("Form", "Remove Entry", None))
        self.singularLabel_3.setText(_translate("Form", "Conjunction:", None))
        self.singularSyllablesLabel_3.setText(_translate("Form", "Conjunction Syllables:", None))
        self.relativeLabel.setText(_translate("Form", "Relative:", None))
        self.relativeSyllablesLabel.setText(_translate("Form", "Relative Syllables:", None))
        self.rCoord.setText(_translate("Form", "Coordinating", None))
        self.rSub.setText(_translate("Form", "Subordinating", None))
        self.bAdd.setText(_translate("Form", "Add/Update Entry", None))

