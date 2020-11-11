function evaluate(settings) {
    let result = "";
    let elements = settings.get('>');
    elements.forEach(function(element){
        if (element.get('element4') != null) {
            let items = element.get('element4').get('>');
            items.forEach(function(item) {
                result += item.get('item').get('value');
            })
        }
    })
    return result;
}