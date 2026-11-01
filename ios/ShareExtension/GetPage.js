var ExtensionPreprocessingJS = new function() {
  this.run = function(arguments) {
    var selection = window.getSelection().toString();
    var root = document.querySelector('article') || document.querySelector('main') || document.body;
    arguments.completionFunction({text: (selection || root.innerText || '').slice(0,100000)});
  };
};
