package jo.carinfo

class CarsListGraph: CarsList(){

    private val hiddenCars = ArrayList<String>()

    private fun hideCar(aCar: Car) {
        if (hiddenCars.indexOf(aCar.mName) == -1)
            hiddenCars.add(aCar.mName)
    }

    private fun showCar(aCar: Car) {
        if (hiddenCars.indexOf(aCar.mName) != -1)
            hiddenCars.removeAt(hiddenCars.indexOf(aCar.mName))
    }

    fun isCarHidden(aCar: Car): Boolean {
        return (hiddenCars.indexOf(aCar.mName) != -1)
    }

    fun processGraphLineVisibility(aCar: Car, aIsChecked: Boolean) {
        if (aIsChecked)
            showCar(aCar)
        else
            hideCar(aCar)
    }
}
