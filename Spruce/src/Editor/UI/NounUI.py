# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Noun.ui'
#
# Created: Sat Feb  5 14:25:12 2011
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

class Ui_Form(object):
    def setupUi(self, Form):
        Form.setObjectName("Form")
        Form.resize(598, 475)
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
        self.verticalLayout = QtGui.QVBoxLayout(self.verticalFrame)
        self.verticalLayout.setMargin(0)
        self.verticalLayout.setObjectName("verticalLayout")
        self.label = QtGui.QLabel(self.verticalFrame)
        self.label.setMaximumSize(QtCore.QSize(200, 16777215))
        self.label.setObjectName("label")
        self.verticalLayout.addWidget(self.label)
        self.lWords = QtGui.QListWidget(self.verticalFrame)
        self.lWords.setMaximumSize(QtCore.QSize(200, 16777215))
        self.lWords.setObjectName("lWords")
        self.verticalLayout.addWidget(self.lWords)
        self.bRemove = QtGui.QPushButton(self.verticalFrame)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.bRemove.sizePolicy().hasHeightForWidth())
        self.bRemove.setSizePolicy(sizePolicy)
        self.bRemove.setObjectName("bRemove")
        self.verticalLayout.addWidget(self.bRemove)
        self.horizontalLayout.addWidget(self.verticalFrame)
        self.line = QtGui.QFrame(Form)
        self.line.setFrameShape(QtGui.QFrame.VLine)
        self.line.setFrameShadow(QtGui.QFrame.Sunken)
        self.line.setObjectName("line")
        self.horizontalLayout.addWidget(self.line)
        self.gridLayout = QtGui.QGridLayout()
        self.gridLayout.setObjectName("gridLayout")
        self.singularLabel = QtGui.QLabel(Form)
        self.singularLabel.setObjectName("singularLabel")
        self.gridLayout.addWidget(self.singularLabel, 0, 0, 1, 1)
        self.tSingular = QtGui.QLineEdit(Form)
        self.tSingular.setObjectName("tSingular")
        self.gridLayout.addWidget(self.tSingular, 0, 1, 1, 1)
        self.singularSyllablesLabel = QtGui.QLabel(Form)
        self.singularSyllablesLabel.setObjectName("singularSyllablesLabel")
        self.gridLayout.addWidget(self.singularSyllablesLabel, 1, 0, 1, 1)
        self.singularSyl = QtGui.QSpinBox(Form)
        self.singularSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.singularSyl.setObjectName("singularSyl")
        self.gridLayout.addWidget(self.singularSyl, 1, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem, 2, 1, 1, 1)
        self.pluralLabel = QtGui.QLabel(Form)
        self.pluralLabel.setObjectName("pluralLabel")
        self.gridLayout.addWidget(self.pluralLabel, 3, 0, 1, 1)
        self.tPlural = QtGui.QLineEdit(Form)
        self.tPlural.setObjectName("tPlural")
        self.gridLayout.addWidget(self.tPlural, 3, 1, 1, 1)
        self.pluralSyllablesLabel = QtGui.QLabel(Form)
        self.pluralSyllablesLabel.setObjectName("pluralSyllablesLabel")
        self.gridLayout.addWidget(self.pluralSyllablesLabel, 4, 0, 1, 1)
        self.pluralSyl = QtGui.QSpinBox(Form)
        self.pluralSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.pluralSyl.setObjectName("pluralSyl")
        self.gridLayout.addWidget(self.pluralSyl, 4, 1, 1, 1)
        spacerItem1 = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem1, 5, 1, 1, 1)
        self.cPlural = QtGui.QCheckBox(Form)
        self.cPlural.setObjectName("cPlural")
        self.gridLayout.addWidget(self.cPlural, 6, 1, 1, 1)
        self.cMass = QtGui.QCheckBox(Form)
        self.cMass.setObjectName("cMass")
        self.gridLayout.addWidget(self.cMass, 7, 1, 1, 1)
        spacerItem2 = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem2, 8, 1, 1, 1)
        self.cMasculine = QtGui.QCheckBox(Form)
        self.cMasculine.setObjectName("cMasculine")
        self.gridLayout.addWidget(self.cMasculine, 9, 1, 1, 1)
        self.cFeminine = QtGui.QCheckBox(Form)
        self.cFeminine.setObjectName("cFeminine")
        self.gridLayout.addWidget(self.cFeminine, 10, 1, 1, 1)
        spacerItem3 = QtGui.QSpacerItem(20, 2000, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout.addItem(spacerItem3, 11, 1, 1, 1)
        self.horizontalLayout_3 = QtGui.QHBoxLayout()
        self.horizontalLayout_3.setObjectName("horizontalLayout_3")
        spacerItem4 = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_3.addItem(spacerItem4)
        self.bAdd = QtGui.QPushButton(Form)
        self.bAdd.setObjectName("bAdd")
        self.horizontalLayout_3.addWidget(self.bAdd)
        self.gridLayout.addLayout(self.horizontalLayout_3, 12, 1, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(Form)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        Form.setWindowTitle(QtGui.QApplication.translate("Form", "Nouns", None, QtGui.QApplication.UnicodeUTF8))
        self.label.setText(QtGui.QApplication.translate("Form", "Nouns (Singlular):", None, QtGui.QApplication.UnicodeUTF8))
        self.bRemove.setText(QtGui.QApplication.translate("Form", "Remove Entry", None, QtGui.QApplication.UnicodeUTF8))
        self.singularLabel.setText(QtGui.QApplication.translate("Form", "Singular", None, QtGui.QApplication.UnicodeUTF8))
        self.singularSyllablesLabel.setText(QtGui.QApplication.translate("Form", "Singular Syllables:", None, QtGui.QApplication.UnicodeUTF8))
        self.pluralLabel.setText(QtGui.QApplication.translate("Form", "Plural", None, QtGui.QApplication.UnicodeUTF8))
        self.pluralSyllablesLabel.setText(QtGui.QApplication.translate("Form", "Plural Syllables:", None, QtGui.QApplication.UnicodeUTF8))
        self.cPlural.setText(QtGui.QApplication.translate("Form", "No Plural", None, QtGui.QApplication.UnicodeUTF8))
        self.cMass.setText(QtGui.QApplication.translate("Form", "Mass Noun", None, QtGui.QApplication.UnicodeUTF8))
        self.cMasculine.setText(QtGui.QApplication.translate("Form", "Masculine", None, QtGui.QApplication.UnicodeUTF8))
        self.cFeminine.setText(QtGui.QApplication.translate("Form", "Feminine", None, QtGui.QApplication.UnicodeUTF8))
        self.bAdd.setText(QtGui.QApplication.translate("Form", "Add/Update Entry", None, QtGui.QApplication.UnicodeUTF8))

