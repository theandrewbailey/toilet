# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Relative.ui'
#
# Created: Sun Jul 28 07:33:51 2013
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

class Ui_Relative(object):
    def setupUi(self, Relative):
        Relative.setObjectName(_fromUtf8("Relative"))
        Relative.resize(500, 188)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(Relative.sizePolicy().hasHeightForWidth())
        Relative.setSizePolicy(sizePolicy)
        self.gridLayout_2 = QtGui.QGridLayout(Relative)
        self.gridLayout_2.setObjectName(_fromUtf8("gridLayout_2"))
        self.cAdverb = QtGui.QCheckBox(Relative)
        self.cAdverb.setObjectName(_fromUtf8("cAdverb"))
        self.gridLayout_2.addWidget(self.cAdverb, 3, 0, 1, 1)
        self.tAdverb = QtGui.QLineEdit(Relative)
        self.tAdverb.setObjectName(_fromUtf8("tAdverb"))
        self.gridLayout_2.addWidget(self.tAdverb, 3, 1, 1, 1)
        self.cAdjective = QtGui.QCheckBox(Relative)
        self.cAdjective.setObjectName(_fromUtf8("cAdjective"))
        self.gridLayout_2.addWidget(self.cAdjective, 2, 0, 1, 1)
        self.cNoun = QtGui.QCheckBox(Relative)
        self.cNoun.setObjectName(_fromUtf8("cNoun"))
        self.gridLayout_2.addWidget(self.cNoun, 0, 0, 1, 1)
        self.cVerb = QtGui.QCheckBox(Relative)
        self.cVerb.setObjectName(_fromUtf8("cVerb"))
        self.gridLayout_2.addWidget(self.cVerb, 1, 0, 1, 1)
        self.tInterj = QtGui.QLineEdit(Relative)
        self.tInterj.setObjectName(_fromUtf8("tInterj"))
        self.gridLayout_2.addWidget(self.tInterj, 4, 1, 1, 1)
        self.cInterj = QtGui.QCheckBox(Relative)
        self.cInterj.setObjectName(_fromUtf8("cInterj"))
        self.gridLayout_2.addWidget(self.cInterj, 4, 0, 1, 1)
        self.tVerb = QtGui.QLineEdit(Relative)
        self.tVerb.setObjectName(_fromUtf8("tVerb"))
        self.gridLayout_2.addWidget(self.tVerb, 1, 1, 1, 1)
        self.tNoun = QtGui.QLineEdit(Relative)
        self.tNoun.setObjectName(_fromUtf8("tNoun"))
        self.gridLayout_2.addWidget(self.tNoun, 0, 1, 1, 1)
        self.tAdjective = QtGui.QLineEdit(Relative)
        self.tAdjective.setObjectName(_fromUtf8("tAdjective"))
        self.gridLayout_2.addWidget(self.tAdjective, 2, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(3, 3, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout_2.addItem(spacerItem, 5, 0, 1, 1)
        self.horizontalLayout = QtGui.QHBoxLayout()
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        spacerItem1 = QtGui.QSpacerItem(3, 3, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout.addItem(spacerItem1)
        self.bCancel = QtGui.QPushButton(Relative)
        self.bCancel.setObjectName(_fromUtf8("bCancel"))
        self.horizontalLayout.addWidget(self.bCancel)
        self.bSave = QtGui.QPushButton(Relative)
        self.bSave.setObjectName(_fromUtf8("bSave"))
        self.horizontalLayout.addWidget(self.bSave)
        self.gridLayout_2.addLayout(self.horizontalLayout, 5, 1, 1, 1)

        self.retranslateUi(Relative)
        QtCore.QObject.connect(self.cAdjective, QtCore.SIGNAL(_fromUtf8("toggled(bool)")), self.tAdjective.setEnabled)
        QtCore.QObject.connect(self.cInterj, QtCore.SIGNAL(_fromUtf8("toggled(bool)")), self.tInterj.setEnabled)
        QtCore.QObject.connect(self.cNoun, QtCore.SIGNAL(_fromUtf8("toggled(bool)")), self.tNoun.setEnabled)
        QtCore.QObject.connect(self.cAdverb, QtCore.SIGNAL(_fromUtf8("toggled(bool)")), self.tAdverb.setEnabled)
        QtCore.QObject.connect(self.cVerb, QtCore.SIGNAL(_fromUtf8("toggled(bool)")), self.tVerb.setEnabled)
        QtCore.QMetaObject.connectSlotsByName(Relative)

    def retranslateUi(self, Relative):
        Relative.setWindowTitle(_translate("Relative", "Dialog", None))
        self.cAdverb.setText(_translate("Relative", "Adverb", None))
        self.cAdjective.setText(_translate("Relative", "Adjective", None))
        self.cNoun.setText(_translate("Relative", "Noun", None))
        self.cVerb.setText(_translate("Relative", "Verb", None))
        self.cInterj.setText(_translate("Relative", "Interjection", None))
        self.bCancel.setText(_translate("Relative", "Don\'t Save", None))
        self.bSave.setText(_translate("Relative", "Save", None))

