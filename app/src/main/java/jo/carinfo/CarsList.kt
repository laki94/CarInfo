package jo.carinfo

import java.io.Serializable

class CarsList: ArrayList<Car>(), Serializable
{
    fun getCarWithName(aName: String): Car?
    {
        for (car in this)
        {
            if (car.mName.equals(aName, true))
                return car
        }
        return null
    }
}
