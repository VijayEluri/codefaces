/*
Syntax highlighting with language autodetection.
http://softwaremaniacs.org/soft/highlight/
*/

var hljs = new function() {
  var LANGUAGES = {}
  // selected_languages is used to support legacy mode of selecting languages
  // available for highlighting by passing them as arguments into
  // initHighlighting function. Currently the whole library is expected to
  // contain only those language definitions that are actually get used.
  var selected_languages = {};
  var NO_HIGHLIGHT = 'no-highlight'

  /* Utility functions */

  function escape(value) {
    return value.replace(/&/gm, '&amp;').replace(/</gm, '&lt;').replace(/>/gm, '&gt;');
  }

  function contains(array, item) {
    if (!array)
      return false;
    for (var i = 0; i < array.length; i++)
      if (array[i] == item)
        return true;
    return false;
  }

  function langRe(language, value, global) {
    var mode =  'm' + (language.case_insensitive ? 'i' : '') + (global ? 'g' : '');
    return new RegExp(value, mode);
  }

  function findCode(pre) {
    for (var i = 0; i < pre.childNodes.length; i++) {
      node = pre.childNodes[i];
      if (node.nodeName == 'CODE')
        return node;
      if (!(node.nodeType == 3 && node.nodeValue.match(/\s+/)))
        return null;
    }
  }

  function blockText(block) {
    var result = '';
    for (var i = 0; i < block.childNodes.length; i++)
      if (block.childNodes[i].nodeType == 3)
        result += block.childNodes[i].nodeValue;
      else if (block.childNodes[i].nodeName == 'BR')
        result += '\n';
      else
        result += blockText(block.childNodes[i]);
    return result;
  }

  function blockLanguage(block) {
    var classes = block.className.split(/\s+/)
    classes = classes.concat(block.parentNode.className.split(/\s+/));
    for (var i = 0; i < classes.length; i++) {
      var class_ = classes[i].replace(/^language-/, '');
      if (class_ == NO_HIGHLIGHT) {
        return NO_HIGHLIGHT;
      }
      if (LANGUAGES[class_]) {
        return class_;
      }
    }
  }

  /* Stream merging */

  function nodeStream(node) {
    var result = [];
    (function (node, offset) {
      for (var i = 0; i < node.childNodes.length; i++) {
        if (node.childNodes[i].nodeType == 3)
          offset += node.childNodes[i].nodeValue.length;
        else if (node.childNodes[i].nodeName == 'BR')
          offset += 1
        else {
          result.push({
            event: 'start',
            offset: offset,
            node: node.childNodes[i]
          });
          offset = arguments.callee(node.childNodes[i], offset)
          result.push({
            event: 'stop',
            offset: offset,
            node: node.childNodes[i]
          });
        }
      }
      return offset;
    })(node, 0);
    return result;
  }

  function mergeStreams(stream1, stream2, value) {
    var processed = 0;
    var result = '';
    var nodeStack = [];

    function selectStream() {
      if (stream1.length && stream2.length) {
        if (stream1[0].offset != stream2[0].offset)
          return (stream1[0].offset < stream2[0].offset) ? stream1 : stream2;
        else
          return (stream1[0].event == 'start' && stream2[0].event == 'stop') ? stream2 : stream1;
      } else {
        return stream1.length ? stream1 : stream2;
      }
    }

    function open(node) {
      var result = '<' + node.nodeName.toLowerCase();
      for (var i = 0; i < node.attributes.length; i++) {
        var attribute = node.attributes[i];
        result += ' ' + attribute.nodeName.toLowerCase();
        if (attribute.nodeValue != undefined) {
          result += '="' + escape(attribute.nodeValue) + '"';
        }
      }
      return result + '>';
    }

    function close(node) {
      return '</' + node.nodeName.toLowerCase() + '>';
    }

    while (stream1.length || stream2.length) {
      var current = selectStream().splice(0, 1)[0];
      result += escape(value.substr(processed, current.offset - processed));
      processed = current.offset;
      if ( current.event == 'start') {
        result += open(current.node);
        nodeStack.push(current.node);
      } else if (current.event == 'stop') {
        var i = nodeStack.length;
        do {
          i--;
          var node = nodeStack[i];
          result += close(node);
        } while (node != current.node);
        nodeStack.splice(i, 1);
        while (i < nodeStack.length) {
          result += open(nodeStack[i]);
          i++;
        }
      }
    }
    result += value.substr(processed);
    return result;
  }

  /* Core highlighting function */

  function highlight(language_name, value) {
    function compileSubModes(mode, language) {
      mode.sub_modes = [];
      for (var i = 0; i < mode.contains.length; i++) {
        for (var j = 0; j < language.modes.length; j++) {
          if (language.modes[j].className == mode.contains[i]) {
            mode.sub_modes[mode.sub_modes.length] = language.modes[j];
          }
        }
      }
    }

    function subMode(lexem, mode) {
      if (!mode.contains) {
        return null;
      }
      if (!mode.sub_modes) {
        compileSubModes(mode, language);
      }
      for (var i = 0; i < mode.sub_modes.length; i++) {
        if (mode.sub_modes[i].beginRe.test(lexem)) {
          return mode.sub_modes[i];
        }
      }
      return null;
    }

    function endOfMode(mode_index, lexem) {
      if (modes[mode_index].end && modes[mode_index].endRe.test(lexem))
        return 1;
      if (modes[mode_index].endsWithParent) {
        var level = endOfMode(mode_index - 1, lexem);
        return level ? level + 1 : 0;
      }
      return 0;
    }

    function isIllegal(lexem, mode) {
      return mode.illegalRe && mode.illegalRe.test(lexem);
    }

    function compileTerminators(mode, language) {
      var terminators = [];

      function addTerminator(re) {
        if (!contains(terminators, re)) {
          terminators[terminators.length] = re;
        }
      }

      if (mode.contains)
        for (var i = 0; i < language.modes.length; i++) {
          if (contains(mode.contains, language.modes[i].className)) {
            addTerminator(language.modes[i].begin);
          }
        }

      var index = modes.length - 1;
      do {
        if (modes[index].end) {
          addTerminator(modes[index].end);
        }
        index--;
      } while (modes[index + 1].endsWithParent);

      if (mode.illegal) {
        addTerminator(mode.illegal);
      }

      var terminator_re = '(' + terminators[0];
      for (var i = 0; i < terminators.length; i++)
        terminator_re += '|' + terminators[i];
      terminator_re += ')';
      return langRe(language, terminator_re);
    }

    function eatModeChunk(value, index) {
      var mode = modes[modes.length - 1];
      if (!mode.terminators) {
        mode.terminators = compileTerminators(mode, language);
      }
      value = value.substr(index);
      var match = mode.terminators.exec(value);
      if (!match)
        return [value, '', true];
      if (match.index == 0)
        return ['', match[0], false];
      else
        return [value.substr(0, match.index), match[0], false];
    }

    function keywordMatch(mode, match) {
      var match_str = language.case_insensitive ? match[0].toLowerCase() : match[0]
      for (var className in mode.keywordGroups) {
        if (!mode.keywordGroups.hasOwnProperty(className))
          continue;
        var value = mode.keywordGroups[className].hasOwnProperty(match_str);
        if (value)
          return [className, value];
      }
      return false;
    }

    function processKeywords(buffer, mode) {
      if (!mode.keywords || !mode.lexems)
        return escape(buffer);
      if (!mode.lexemsRe) {
        var lexems_re = '(' + mode.lexems[0];
        for (var i = 1; i < mode.lexems.length; i++)
          lexems_re += '|' + mode.lexems[i];
        lexems_re += ')';
        mode.lexemsRe = langRe(language, lexems_re, true);
      }
      var result = '';
      var last_index = 0;
      mode.lexemsRe.lastIndex = 0;
      var match = mode.lexemsRe.exec(buffer);
      while (match) {
        result += escape(buffer.substr(last_index, match.index - last_index));
        var keyword_match = keywordMatch(mode, match);
        if (keyword_match) {
          keyword_count += keyword_match[1];
          result += '<span class="'+ keyword_match[0] +'">' + escape(match[0]) + '</span>';
        } else {
          result += escape(match[0]);
        }
        last_index = mode.lexemsRe.lastIndex;
        match = mode.lexemsRe.exec(buffer);
      }
      result += escape(buffer.substr(last_index, buffer.length - last_index));
      return result;
    }

    function processBuffer(buffer, mode) {
      if (mode.subLanguage && selected_languages[mode.subLanguage]) {
        var result = highlight(mode.subLanguage, buffer);
        keyword_count += result.keyword_count;
        relevance += result.relevance;
        return result.value;
      } else {
        return processKeywords(buffer, mode);
      }
    }

    function startNewMode(mode, lexem) {
      var markup = mode.noMarkup?'':'<span class="' + mode.displayClassName + '">';
      if (mode.returnBegin) {
        result += markup;
        mode.buffer = '';
      } else if (mode.excludeBegin) {
        result += escape(lexem) + markup;
        mode.buffer = '';
      } else {
        result += markup;
        mode.buffer = lexem;
      }
      modes[modes.length] = mode;
    }

    function processModeInfo(buffer, lexem, end) {
      var current_mode = modes[modes.length - 1];
      if (end) {
        result += processBuffer(current_mode.buffer + buffer, current_mode);
        return false;
      }

      var new_mode = subMode(lexem, current_mode);
      if (new_mode) {
        result += processBuffer(current_mode.buffer + buffer, current_mode);
        startNewMode(new_mode, lexem);
        relevance += new_mode.relevance;
        return new_mode.returnBegin;
      }

      var end_level = endOfMode(modes.length - 1, lexem);
      if (end_level) {
        var markup = current_mode.noMarkup?'':'</span>';
        if (current_mode.returnEnd) {
          result += processBuffer(current_mode.buffer + buffer, current_mode) + markup;
        } else if (current_mode.excludeEnd) {
          result += processBuffer(current_mode.buffer + buffer, current_mode) + markup + escape(lexem);
        } else {
          result += processBuffer(current_mode.buffer + buffer + lexem, current_mode) + markup;
        }
        while (end_level > 1) {
          markup = modes[modes.length - 2].noMarkup?'':'</span>';
          result += markup;
          end_level--;
          modes.length--;
        }
        modes.length--;
        modes[modes.length - 1].buffer = '';
        if (current_mode.starts) {
          for (var i = 0; i < language.modes.length; i++) {
            if (language.modes[i].className == current_mode.starts) {
              startNewMode(language.modes[i], '');
              break;
            }
          }
        }
        return current_mode.returnEnd;
      }

      if (isIllegal(lexem, current_mode))
        throw 'Illegal';
    }

    var language = LANGUAGES[language_name];
    var modes = [language.defaultMode];
    var relevance = 0;
    var keyword_count = 0;
    var result = '';
    try {
      var index = 0;
      language.defaultMode.buffer = '';
      do {
        var mode_info = eatModeChunk(value, index);
        var return_lexem = processModeInfo(mode_info[0], mode_info[1], mode_info[2]);
        index += mode_info[0].length;
        if (!return_lexem) {
          index += mode_info[1].length;
        }
      } while (!mode_info[2]);
      if(modes.length > 1)
        throw 'Illegal';
      return {
        relevance: relevance,
        keyword_count: keyword_count,
        value: result
      }
    } catch (e) {
      if (e == 'Illegal') {
        return {
          relevance: 0,
          keyword_count: 0,
          value: escape(value)
        }
      } else {
        throw e;
      }
    }
  }

  /* Initialization */

  function compileModes() {
    for (var i in LANGUAGES) {
      if (!LANGUAGES.hasOwnProperty(i))
        continue;
      var language = LANGUAGES[i];
      for (var j = 0; j < language.modes.length; j++) {
        var mode = language.modes[j];
        if (mode.begin)
          mode.beginRe = langRe(language, '^' + mode.begin);
        if (mode.end)
          mode.endRe = langRe(language, '^' + mode.end);
        if (mode.illegal)
          mode.illegalRe = langRe(language, '^(?:' + mode.illegal + ')');
        language.defaultMode.illegalRe = langRe(language, '^(?:' + language.defaultMode.illegal + ')');
        if (mode.relevance == undefined) {
          mode.relevance = 1;
        }
        if (!mode.displayClassName) {
          mode.displayClassName = mode.className;
        }
      }
    }
  }

  function compileKeywords() {

    function compileModeKeywords(mode) {
      if (!mode.keywordGroups) {
        for (var key in mode.keywords) {
          if (!mode.keywords.hasOwnProperty(key))
            continue;
          if (mode.keywords[key] instanceof Object)
            mode.keywordGroups = mode.keywords;
          else
            mode.keywordGroups = {'keyword': mode.keywords};
          break;
        }
      }
    }

    for (var i in LANGUAGES) {
      if (!LANGUAGES.hasOwnProperty(i))
        continue;
      var language = LANGUAGES[i];
      compileModeKeywords(language.defaultMode);
      for (var j = 0; j < language.modes.length; j++) {
        compileModeKeywords(language.modes[j]);
      }
    }
  }

  function initialize() {
    if (initialize.called)
        return;
    initialize.called = true;
    compileModes();
    compileKeywords();
    selected_languages = LANGUAGES;
  }

  /* Public library functions */

  function highlightBlock(block, tabReplace, showLineNumber) {
    initialize();

    try {
      var text = blockText(block);
      var language = blockLanguage(block);
    } catch (e) {
      language = NO_HIGHLIGHT;
    }

    if (language == NO_HIGHLIGHT){
        var result = escape(text);
    }
    else{ /* no highlight */
        /* language is specified */
        var result = highlight(language, text).value;
    }

    if (result) {
      var class_name = block.className;
      if (!class_name.match(language)) {
        class_name += ' ' + language;
      }
      var original = nodeStream(block);
      if (original.length) {
        var pre = document.createElement('pre');
        pre.innerHTML = result;
        result = mergeStreams(original, nodeStream(pre), text);
      }
      if (tabReplace) {
        result = result.replace(/^((<[^>]+>|\t)+)/gm, function(match, p1, offset, s) {
          return p1.replace(/\t/g, tabReplace);
        })
      }
      if(showLineNumber) {
        var m = result.match(/([^\n\r]*)(?:\n\r|\r\n|\r|\n|$)/g);
        var line_no = '<div class="line_number">';
        for(var j=0; j < m.length-1; j++) { line_no += (1+j) + '<br/>'; }
        line_no += '</div>';
      }
      // See these 4 lines? This is IE's notion of "block.innerHTML = result". Love this browser :-/
      var container = document.createElement('div');
      
      if(showLineNumber) {
        container.innerHTML = '<pre><code class="' + class_name + '">' + line_no + '<div class="codeContent">' + result + '</div>' + '</code></pre>';
      } else {
        container.innerHTML = '<pre><code class="' + class_name + '">' + result + '</code></pre>';
      }

      var environment = block.parentNode.parentNode;
      environment.replaceChild(container.firstChild, block.parentNode);
    }
  }

  function initHighlighting() {
    if (initHighlighting.called)
      return;
    initHighlighting.called = true;
    initialize();
    if (arguments.length) {
      for (var i = 0; i < arguments.length; i++) {
        if (LANGUAGES[arguments[i]]) {
          selected_languages[arguments[i]] = LANGUAGES[arguments[i]];
        }
      }
    }
    var pres = document.getElementsByTagName('pre');
    for (var i = 0; i < pres.length; i++) {
      var code = findCode(pres[i]);
      if (code)
        highlightBlock(code, hljs.tabReplace, hljs.showLineNumber);
    }
  }

  function initHighlightingOnLoad() {
    var original_arguments = arguments;
    var handler = function(){initHighlighting.apply(null, original_arguments)};
    if (window.addEventListener) {
      window.addEventListener('DOMContentLoaded', handler, false);
      window.addEventListener('load', handler, false);
    } else if (window.attachEvent)
      window.attachEvent('onload', handler);
    else
      window.onload = handler;
  }

  /* Interface definition */

  this.LANGUAGES = LANGUAGES;
  this.initHighlightingOnLoad = initHighlightingOnLoad;
  this.highlightBlock = highlightBlock;
  this.initHighlighting = initHighlighting;

  // Common regexps
  this.IDENT_RE = '[a-zA-Z][a-zA-Z0-9_]*';
  this.UNDERSCORE_IDENT_RE = '[a-zA-Z_][a-zA-Z0-9_]*';
  this.NUMBER_RE = '\\b\\d+(\\.\\d+)?';
  this.C_NUMBER_RE = '\\b(0x[A-Za-z0-9]+|\\d+(\\.\\d+)?)';
  this.RE_STARTERS_RE = '!|!=|!==|%|%=|&|&&|&=|\\*|\\*=|\\+|\\+=|,|\\.|-|-=|/|/=|:|;|<|<<|<<=|<=|=|==|===|>|>=|>>|>>=|>>>|>>>=|\\?|\\[|\\{|\\(|\\^|\\^=|\\||\\|=|\\|\\||~';

  // Common modes
  this.APOS_STRING_MODE = {
    className: 'string',
    begin: '\'', end: '\'',
    illegal: '\\n',
    contains: ['escape'],
    relevance: 0
  };
  this.QUOTE_STRING_MODE = {
    className: 'string',
    begin: '"', end: '"',
    illegal: '\\n',
    contains: ['escape'],
    relevance: 0
  };
  this.BACKSLASH_ESCAPE = {
    className: 'escape',
    begin: '\\\\.', end: '^', noMarkup: true,
    relevance: 0
  };
  this.C_LINE_COMMENT_MODE = {
    className: 'comment',
    begin: '//', end: '$',
    relevance: 0
  };
  this.C_BLOCK_COMMENT_MODE = {
    className: 'comment',
    begin: '/\\*', end: '\\*/'
  };
  this.HASH_COMMENT_MODE = {
    className: 'comment',
    begin: '#', end: '$'
  };
  this.C_NUMBER_MODE = {
    className: 'number',
    begin: this.C_NUMBER_RE, end: '^',
    relevance: 0
  };
}();

var initHighlightingOnLoad = hljs.initHighlightingOnLoad;
