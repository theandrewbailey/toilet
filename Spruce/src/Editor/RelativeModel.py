import Editor.UI.RelativeUI as relUI
from PyQt4 import QtCore, QtGui

class RelativeModel(relUI.Ui_Relative):
    """A window that allows entry of many, many words of one part of speech at once"""
    def setupUi(self):
        self.relContainer=QtGui.QDialog()

        # stuff, heh
        relUI.Ui_Relative.setupUi(self, self.relContainer)
        QtCore.QObject.connect(self.bSave, QtCore.SIGNAL("clicked()"), self.save)
        QtCore.QObject.connect(self.bCancel, QtCore.SIGNAL("clicked()"), self.cancel)

        self.relContainer.show()

    def save(self):
        pass

    def cancel(self):
        pass
    