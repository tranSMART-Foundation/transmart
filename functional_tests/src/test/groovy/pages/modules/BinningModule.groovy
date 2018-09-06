package pages.modules

import geb.Module

class BinningModule extends Module {

    static content = {
        binningCheckbox { $('input[type=checkbox][name=isBinning]') }
        numberOfBins    { $('input#txtNumberOfBins') }
        binDistribution { $('select#selBinDistribution') }
    }

    void enableBinning() {
        if (!binningCheckbox.value()) {
            binningCheckbox.click()
        }

        assert binningCheckbox.value()
    }

    void selectEvenlyDistributedPopulation() {
        binDistribution.value('EDP')
    }

    void selectEvenlySpacedBins() {
        binDistribution.value('ESB')
    }

}
