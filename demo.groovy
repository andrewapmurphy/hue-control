input.each { key, light ->
    color = api.toColor(light)

    //if (api.random() < .5L) {
    color = api.toColor(api.random(), api.random(), api.random())
    //} else {
    //color = api.toColor(0, 0, 0)
    //}

    newLight = api.toLightState(color)

    output[key] = newLight
}