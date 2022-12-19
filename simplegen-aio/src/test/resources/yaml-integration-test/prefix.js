const ArrayList = Java.type("java.util.ArrayList")

function prefix(input, resolve, args) {
    let list = [...input].flat()
    let output = new ArrayList()
    list.forEach(it => output.add(args[0] + it))
    return output
}