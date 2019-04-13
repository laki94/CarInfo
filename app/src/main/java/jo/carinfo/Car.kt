package jo.carinfo

import java.io.Serializable

class Car(aName: String = ""): Serializable
{

    var mName : String = aName
}

class CarsList: ArrayList<Car>(),Serializable
{

}