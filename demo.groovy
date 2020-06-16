input.each { key, light ->
    color = api.toColor(light)
    frac = 0.5 * Math.sin(context.timeMs)

    color = api.toColor(api.random() * frac, api.random() * frac, api.random() * frac)

    newLight = api.toLightState(color)

    output[key] = newLight
}