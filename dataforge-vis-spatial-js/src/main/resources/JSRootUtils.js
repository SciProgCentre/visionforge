
/** Generate mask for given bit
 *
 * @param {number} n bit number
 * @returns {Number} produced make
 * @private */
export function BIT(n) {
    return 1 << (n);
}

/** Wrapper for console.log, let redirect output to specified div element
 * @private */
function console(value, divid) {
    if ((typeof divid == 'string') && document.getElementById(divid))
        document.getElementById(divid).innerHTML = value;
    else if ((typeof console != 'undefined') && (typeof console.log == 'function'))
        console.log(value);
}


/** @summary Wrapper for alert, throws Error in Node.js
 * @private */
export function alert(msg) {
    if (this.nodeis) throw new Error(msg);
    if (typeof alert === 'function') alert(msg);
    else console('ALERT: ' + msg);
}

/**
 * @summary Seed simple random generator
 *
 * @private
 * @param {number} i seed value
 */
export function seed(i) {
    i = Math.abs(i);
    if (i > 1e8) i = Math.abs(1e8 * Math.sin(i)); else if (i < 1) i *= 1e8;
    this.m_w = Math.round(i);
    this.m_z = 987654321;
}

/**
 * @summary Simple random generator
 *
 * @desc Works like Math.random(), but with configurable seed - see {@link JSROOT.seed}
 * @private
 * @returns {number} random value between 0 (inclusive) and 1.0 (exclusive)
 */
export function random() {
    if (this.m_z === undefined) return Math.random();
    this.m_z = (36969 * (this.m_z & 65535) + (this.m_z >> 16)) & 0xffffffff;
    this.m_w = (18000 * (this.m_w & 65535) + (this.m_w >> 16)) & 0xffffffff;
    var result = ((this.m_z << 16) + this.m_w) & 0xffffffff;
    result /= 4294967296;
    return result + 0.5;
}


/** @summary Should be used to reintroduce objects references, produced by TBufferJSON.
 *
 * @desc Replace all references inside object, object should not be null
 * Idea of the code taken from JSON-R code, found on
 * https://github.com/graniteds/jsonr
 * Only unref part was used, arrays are not accounted as objects
 * @param {object} obj  object where references will be replaced
 * @returns {object} same object with replaced references
 * @private */
function JSONR_unref(obj) {

    let map = [], newfmt = undefined;

    function unref_value(value) {
        if ((value === null) || (value === undefined)) return;

        /*
            if object is a reference string in "old format"
            Old format seems to be single string with "$ref:" prefix. New format is an object
         */
        if (typeof value === 'string') {
            if (newfmt || (value.length < 6) || (value.indexOf("$ref:") !== 0)) return; //switch to "new format" if needed
            let ref = parseInt(value.substr(5)); // get ref number
            if (isNaN(ref) || (ref < 0) || (ref >= map.length)) return; //skip if not a ref
            newfmt = false;
            return map[ref]; //return an object from cache
        }

        if (typeof value !== 'object') return;

        let i, k, res, proto = Object.prototype.toString.apply(value);

        // scan array - it can contain other objects
        if ((proto.indexOf('[object') === 0) && (proto.indexOf('Array]') > 0)) {
            for (i = 0; i < value.length; ++i) {
                res = unref_value(value[i]);
                if (res !== undefined) value[i] = res;
            }
            return;
        }

        let ks = Object.keys(value), len = ks.length;

        if ((newfmt !== false) && (len === 1) && (ks[0] === '$ref')) {
            let ref = parseInt(value['$ref']);
            if (isNaN(ref) || (ref < 0) || (ref >= map.length)) return;
            newfmt = true;
            return map[ref];
        }

        if ((newfmt !== false) && (len > 1) && (ks[0] === '$arr') && (ks[1] === 'len')) {
            // this is ROOT-coded array
            var arr = null, dflt = (value.$arr === "Bool") ? false : 0;
            switch (value.$arr) {
                case "Int8" :
                    arr = new Int8Array(value.len);
                    break;
                case "Uint8" :
                    arr = new Uint8Array(value.len);
                    break;
                case "Int16" :
                    arr = new Int16Array(value.len);
                    break;
                case "Uint16" :
                    arr = new Uint16Array(value.len);
                    break;
                case "Int32" :
                    arr = new Int32Array(value.len);
                    break;
                case "Uint32" :
                    arr = new Uint32Array(value.len);
                    break;
                case "Float32" :
                    arr = new Float32Array(value.len);
                    break;
                case "Int64" :
                case "Uint64" :
                case "Float64" :
                    arr = new Float64Array(value.len);
                    break;
                default :
                    arr = new Array(value.len);
                    break;
            }
            for (let k = 0; k < value.len; ++k) arr[k] = dflt;

            var nkey = 2, p = 0;
            while (nkey < len) {
                if (ks[nkey][0] === "p") p = value[ks[nkey++]]; // position
                if (ks[nkey][0] !== 'v') throw new Error('Unexpected member ' + ks[nkey] + ' in array decoding');
                let v = value[ks[nkey++]]; // value
                if (typeof v === 'object') {
                    for (let k = 0; k < v.length; ++k) arr[p++] = v[k];
                } else {
                    arr[p++] = v;
                    if ((nkey < len) && (ks[nkey][0] === 'n')) {
                        let cnt = value[ks[nkey++]]; // counter
                        while (--cnt) arr[p++] = v;
                    }
                }
            }

            return arr;
        }

        if ((newfmt !== false) && (len === 3) && (ks[0] === '$pair') && (ks[1] === 'first') && (ks[2] === 'second')) {
            newfmt = true;
            let f1 = unref_value(value.first),
                s1 = unref_value(value.second);
            if (f1 !== undefined) value.first = f1;
            if (s1 !== undefined) value.second = s1;
            value._typename = value['$pair'];
            delete value['$pair'];
            return; // pair object is not counted in the objects map
        }

        // debug code, can be commented out later
        if (map.indexOf(value) >= 0) {
            console('should never happen - object already in the map');
            return;
        }

        // add object to object map
        map.push(value);

        // add methods to all objects, where _typename is specified
        //if ('_typename' in value) JSROOT.addMethods(value);

        for (let k = 0; k < len; ++k) {
            i = ks[k];
            res = unref_value(value[i]);
            if (res !== undefined) value[i] = res;
        }
    }

    unref_value(obj);

    return obj;
}


/** @summary Just copies (not clone) all fields from source to the target object
 * @desc This is simple replacement of jQuery.extend method
 * @private */
function extend(tgt, src) {
    if ((src === null) || (typeof src !== 'object')) return tgt;
    if ((tgt === null) || (typeof tgt !== 'object')) tgt = {};

    for (var k in src)
        tgt[k] = src[k];

    return tgt;
}


/**
 * @summary Parse JSON code produced with TBufferJSON.
 *
 * @param {string} json string to parse
 * @return {object|null} returns parsed object
 */
export function parse(json) {
    if (!json) return null;
    let obj = JSON.parse(json);
    if (obj) obj = JSONR_unref(obj);
    return obj;
}


/**
 * @summary Parse multi.json request results
 * @desc Method should be used to parse JSON code, produced by multi.json request of THttpServer
 *
 * @param {string} json string to parse
 * @return {Array|null} returns array of parsed elements
 */
export function parse_multi(json) {
    if (!json) return null;
    let arr = JSON.parse(json);
    if (arr && arr.length)
        for (let i = 0; i < arr.length; ++i)
            arr[i] = JSONR_unref(arr[i]);
    return arr;
}

/**
 * @summary Method converts JavaScript object into ROOT-like JSON
 *
 * @desc Produced JSON can be used in JSROOT.parse() again
 * When performed properly, JSON can be used in TBufferJSON to read data back with C++
 */
export function toJSON(obj) {
    if (!obj || typeof obj !== 'object') return "";

    var map = []; // map of stored objects

    function copy_value(value) {
        if (typeof value === "function") return undefined;

        if ((value === undefined) || (value === null) || (typeof value !== 'object')) return value;

        var proto = Object.prototype.toString.apply(value);

        // typed array need to be converted into normal array, otherwise looks strange
        if ((proto.indexOf('[object ') === 0) && (proto.indexOf('Array]') === proto.length - 6)) {
            var arr = new Array(value.length);
            for (var i = 0; i < value.length; ++i)
                arr[i] = copy_value(value[i]);
            return arr;
        }

        // this is how reference is code
        var refid = map.indexOf(value);
        if (refid >= 0) return {$ref: refid};

        var ks = Object.keys(value), len = ks.length, tgt = {};

        if ((len === 3) && (ks[0] === '$pair') && (ks[1] === 'first') && (ks[2] === 'second')) {
            // special handling of pair objects which does not included into objects map
            tgt.$pair = value.$pair;
            tgt.first = copy_value(value.first);
            tgt.second = copy_value(value.second);
            return tgt;
        }

        map.push(value);

        for (var k = 0; k < len; ++k) {
            var name = ks[k];
            tgt[name] = copy_value(value[name]);
        }

        return tgt;
    }

    var tgt = copy_value(obj);

    return JSON.stringify(tgt);
}


/**
 * @summary Parse string value as array.
 *
 * @desc It could be just simple string:  "value" or
 * array with or without string quotes:  [element], ['elem1',elem2]
 *
 * @private
 */
function ParseAsArray(val) {

    var res = [];

    if (typeof val != 'string') return res;

    val = val.trim();
    if (val === "") return res;

    // return as array with single element
    if ((val.length < 2) || (val[0] !== '[') || (val[val.length - 1] !== ']')) {
        res.push(val);
        return res;
    }

    // try to split ourself, checking quotes and brackets
    var nbr = 0, nquotes = 0, ndouble = 0, last = 1;

    for (var indx = 1; indx < val.length; ++indx) {
        if (nquotes > 0) {
            if (val[indx] === "'") nquotes--;
            continue;
        }
        if (ndouble > 0) {
            if (val[indx] === '"') ndouble--;
            continue;
        }
        switch (val[indx]) {
            case "'":
                nquotes++;
                break;
            case '"':
                ndouble++;
                break;
            case "[":
                nbr++;
                break;
            case "]":
                if (indx < val.length - 1) {
                    nbr--;
                    break;
                }
            case ",":
                if (nbr === 0) {
                    var sub = val.substring(last, indx).trim();
                    if ((sub.length > 1) && (sub[0] === sub[sub.length - 1]) && ((sub[0] === '"') || (sub[0] === "'")))
                        sub = sub.substr(1, sub.length - 2);
                    res.push(sub);
                    last = indx + 1;
                }
                break;
        }
    }

    if (res.length === 0)
        res.push(val.substr(1, val.length - 2).trim());

    return res;
}

/**
 * @summary Find function with given name.
 *
 * @desc Function name may include several namespaces like 'JSROOT.Painter.drawFrame'
 *
 * @private
 */
function findFunction(name) {
    if (typeof name === 'function') return name;
    if (typeof name !== 'string') return null;
    var names = name.split('.'), elem = null;
    if (typeof window === 'object') elem = window;
    if (names[0] === 'JSROOT') {
        elem = this;
        names.shift();
    }

    for (var n = 0; elem && (n < names.length); ++n)
        elem = elem[names[n]];

    return (typeof elem == 'function') ? elem : null;
}

/**
 * @summary Generic method to invoke callback function.
 *
 * @param {object|function} func either normal function or container like
 * { obj: object_pointer, func: name of method to call }
 * @param arg1 first optional argument of callback
 * @param arg2 second optional argument of callback
 *
 * @private
 */
function callBack(func, arg1, arg2) {

    if (typeof func == 'string') func = findFunction(func);

    if (!func) return;

    if (typeof func == 'function') return func(arg1, arg2);

    if (typeof func != 'object') return;

    if (('obj' in func) && ('func' in func) &&
        (typeof func.obj == 'object') && (typeof func.func == 'string') &&
        (typeof func.obj[func.func] == 'function')) {
        return func.obj[func.func](arg1, arg2);
    }
}

let methodsCache = {}; // variable used to keep methods for known classes


/** @summary Returns methods for given typename
 * @private
 */
function getMethods(typename, obj) {

    var m = methodsCache[typename],
        has_methods = (m !== undefined);

    if (!has_methods) m = {};

    // Due to binary I/O such TObject methods may not be set for derived classes
    // Therefore when methods requested for given object, check also that basic methods are there
    if ((typename === "TObject") || (typename === "TNamed") || (obj && (obj.fBits !== undefined)))
        if (m.TestBit === undefined) {
            m.TestBit = function (f) {
                return (this.fBits & f) !== 0;
            };
            m.InvertBit = function (f) {
                this.fBits = this.fBits ^ (f & 0xffffff);
            };
        }

    if (has_methods) return m;

    if ((typename === 'TList') || (typename === 'THashList')) {
        m.Clear = function () {
            this.arr = [];
            this.opt = [];
        };
        m.Add = function (obj, opt) {
            this.arr.push(obj);
            this.opt.push((opt && typeof opt == 'string') ? opt : "");
        };
        m.AddFirst = function (obj, opt) {
            this.arr.unshift(obj);
            this.opt.unshift((opt && typeof opt == 'string') ? opt : "");
        };
        m.RemoveAt = function (indx) {
            this.arr.splice(indx, 1);
            this.opt.splice(indx, 1);
        }
    }

    // if ((typename === "TPaveText") || (typename === "TPaveStats")) {
    //     m.AddText = function (txt) {
    //         // this.fLines.Add({ _typename: 'TLatex', fTitle: txt, fTextColor: 1 });
    //         var line = JSROOT.Create("TLatex");
    //         line.fTitle = txt;
    //         this.fLines.Add(line);
    //     };
    //     m.Clear = function () {
    //         this.fLines.Clear();
    //     }
    // }
    //
    // if ((typename.indexOf("TF1") === 0) || (typename === "TF2")) {
    //     m.addFormula = function (obj) {
    //         if (!obj) return;
    //         if (this.formulas === undefined) this.formulas = [];
    //         this.formulas.push(obj);
    //     };
    //
    //     m.evalPar = function (x, y) {
    //         if (!('_func' in this) || (this._title !== this.fTitle)) {
    //
    //             var _func = this.fTitle, isformula = false, pprefix = "[";
    //             if (_func === "gaus") _func = "gaus(0)";
    //             if (this.fFormula && typeof this.fFormula.fFormula == "string") {
    //                 if (this.fFormula.fFormula.indexOf("[](double*x,double*p)") === 0) {
    //                     isformula = true;
    //                     pprefix = "p[";
    //                     _func = this.fFormula.fFormula.substr(21);
    //                 } else {
    //                     _func = this.fFormula.fFormula;
    //                     pprefix = "[p";
    //                 }
    //                 if (this.fFormula.fClingParameters && this.fFormula.fParams) {
    //                     for (var i = 0; i < this.fFormula.fParams.length; ++i) {
    //                         var regex = new RegExp('(\\[' + this.fFormula.fParams[i].first + '\\])', 'g'),
    //                             parvalue = this.fFormula.fClingParameters[this.fFormula.fParams[i].second];
    //                         _func = _func.replace(regex, (parvalue < 0) ? "(" + parvalue + ")" : parvalue);
    //                     }
    //                 }
    //             }
    //
    //             if ('formulas' in this)
    //                 for (var i = 0; i < this.formulas.length; ++i)
    //                     while (_func.indexOf(this.formulas[i].fName) >= 0)
    //                         _func = _func.replace(this.formulas[i].fName, this.formulas[i].fTitle);
    //             _func = _func.replace(/\b(abs)\b/g, 'TMath::Abs')
    //                 .replace(/TMath::Exp\(/g, 'Math.exp(')
    //                 .replace(/TMath::Abs\(/g, 'Math.abs(');
    //             if (typeof JSROOT.Math == 'object') {
    //                 this._math = JSROOT.Math;
    //                 _func = _func.replace(/TMath::Prob\(/g, 'this._math.Prob(')
    //                     .replace(/TMath::Gaus\(/g, 'this._math.Gaus(')
    //                     .replace(/TMath::BreitWigner\(/g, 'this._math.BreitWigner(')
    //                     .replace(/xygaus\(/g, 'this._math.gausxy(this, x, y, ')
    //                     .replace(/gaus\(/g, 'this._math.gaus(this, x, ')
    //                     .replace(/gausn\(/g, 'this._math.gausn(this, x, ')
    //                     .replace(/expo\(/g, 'this._math.expo(this, x, ')
    //                     .replace(/landau\(/g, 'this._math.landau(this, x, ')
    //                     .replace(/landaun\(/g, 'this._math.landaun(this, x, ')
    //                     .replace(/ROOT::Math::/g, 'this._math.');
    //             }
    //             for (var i = 0; i < this.fNpar; ++i) {
    //                 var parname = pprefix + i + "]";
    //                 while (_func.indexOf(parname) !== -1)
    //                     _func = _func.replace(parname, '(' + this.GetParValue(i) + ')');
    //             }
    //             _func = _func.replace(/\b(sin)\b/gi, 'Math.sin')
    //                 .replace(/\b(cos)\b/gi, 'Math.cos')
    //                 .replace(/\b(tan)\b/gi, 'Math.tan')
    //                 .replace(/\b(exp)\b/gi, 'Math.exp')
    //                 .replace(/\b(pow)\b/gi, 'Math.pow')
    //                 .replace(/pi/g, 'Math.PI');
    //             for (var n = 2; n < 10; ++n)
    //                 _func = _func.replace('x^' + n, 'Math.pow(x,' + n + ')');
    //
    //             if (isformula) {
    //                 _func = _func.replace(/x\[0\]/g, "x");
    //                 if (this._typename === "TF2") {
    //                     _func = _func.replace(/x\[1\]/g, "y");
    //                     this._func = new Function("x", "y", _func).bind(this);
    //                 } else {
    //                     this._func = new Function("x", _func).bind(this);
    //                 }
    //             } else if (this._typename === "TF2")
    //                 this._func = new Function("x", "y", "return " + _func).bind(this);
    //             else
    //                 this._func = new Function("x", "return " + _func).bind(this);
    //
    //             this._title = this.fTitle;
    //         }
    //
    //         return this._func(x, y);
    //     };
    //     m.GetParName = function (n) {
    //         if (this.fFormula && this.fFormula.fParams) return this.fFormula.fParams[n].first;
    //         if (this.fNames && this.fNames[n]) return this.fNames[n];
    //         return "p" + n;
    //     };
    //     m.GetParValue = function (n) {
    //         if (this.fFormula && this.fFormula.fClingParameters) return this.fFormula.fClingParameters[n];
    //         if (this.fParams) return this.fParams[n];
    //         return undefined;
    //     };
    //     m.GetParError = function (n) {
    //         return this.fParErrors ? this.fParErrors[n] : undefined;
    //     };
    //     m.GetNumPars = function () {
    //         return this.fNpar;
    //     }
    // }

    if (((typename.indexOf("TGraph") === 0) || (typename === "TCutG")) && (typename !== "TGraphPolargram") && (typename !== "TGraphTime")) {
        // check if point inside figure specified by the TGraph
        m.IsInside = function (xp, yp) {
            var i, j = this.fNpoints - 1, x = this.fX, y = this.fY, oddNodes = false;

            for (i = 0; i < this.fNpoints; ++i) {
                if ((y[i] < yp && y[j] >= yp) || (y[j] < yp && y[i] >= yp)) {
                    if (x[i] + (yp - y[i]) / (y[j] - y[i]) * (x[j] - x[i]) < xp) {
                        oddNodes = !oddNodes;
                    }
                }
                j = i;
            }

            return oddNodes;
        };
    }

    if (typename.indexOf("TH1") === 0 ||
        typename.indexOf("TH2") === 0 ||
        typename.indexOf("TH3") === 0) {
        m.getBinError = function (bin) {
            //   -*-*-*-*-*Return value of error associated to bin number bin*-*-*-*-*
            //    if the sum of squares of weights has been defined (via Sumw2),
            //    this function returns the sqrt(sum of w2).
            //    otherwise it returns the sqrt(contents) for this bin.
            if (bin >= this.fNcells) bin = this.fNcells - 1;
            if (bin < 0) bin = 0;
            if (bin < this.fSumw2.length)
                return Math.sqrt(this.fSumw2[bin]);
            return Math.sqrt(Math.abs(this.fArray[bin]));
        };
        m.setBinContent = function (bin, content) {
            // Set bin content - only trivial case, without expansion
            this.fEntries++;
            this.fTsumw = 0;
            if ((bin >= 0) && (bin < this.fArray.length))
                this.fArray[bin] = content;
        };
    }

    if (typename.indexOf("TH1") === 0) {
        m.getBin = function (x) {
            return x;
        };
        m.getBinContent = function (bin) {
            return this.fArray[bin];
        };
        m.Fill = function (x, weight) {
            var axis = this.fXaxis,
                bin = 1 + Math.floor((x - axis.fXmin) / (axis.fXmax - axis.fXmin) * axis.fNbins);
            if (bin < 0) bin = 0; else if (bin > axis.fNbins + 1) bin = axis.fNbins + 1;
            this.fArray[bin] += ((weight === undefined) ? 1 : weight);
        }
    }

    if (typename.indexOf("TH2") === 0) {
        m.getBin = function (x, y) {
            return (x + (this.fXaxis.fNbins + 2) * y);
        };
        m.getBinContent = function (x, y) {
            return this.fArray[this.getBin(x, y)];
        };
        m.Fill = function (x, y, weight) {
            var axis1 = this.fXaxis, axis2 = this.fYaxis,
                bin1 = 1 + Math.floor((x - axis1.fXmin) / (axis1.fXmax - axis1.fXmin) * axis1.fNbins),
                bin2 = 1 + Math.floor((y - axis2.fXmin) / (axis2.fXmax - axis2.fXmin) * axis2.fNbins);
            if (bin1 < 0) bin1 = 0; else if (bin1 > axis1.fNbins + 1) bin1 = axis1.fNbins + 1;
            if (bin2 < 0) bin2 = 0; else if (bin2 > axis2.fNbins + 1) bin2 = axis2.fNbins + 1;
            this.fArray[bin1 + (axis1.fNbins + 2) * bin2] += ((weight === undefined) ? 1 : weight);
        }
    }

    if (typename.indexOf("TH3") === 0) {
        m.getBin = function (x, y, z) {
            return (x + (this.fXaxis.fNbins + 2) * (y + (this.fYaxis.fNbins + 2) * z));
        };
        m.getBinContent = function (x, y, z) {
            return this.fArray[this.getBin(x, y, z)];
        };
        m.Fill = function (x, y, z, weight) {
            var axis1 = this.fXaxis, axis2 = this.fYaxis, axis3 = this.fZaxis,
                bin1 = 1 + Math.floor((x - axis1.fXmin) / (axis1.fXmax - axis1.fXmin) * axis1.fNbins),
                bin2 = 1 + Math.floor((y - axis2.fXmin) / (axis2.fXmax - axis2.fXmin) * axis2.fNbins),
                bin3 = 1 + Math.floor((z - axis3.fXmin) / (axis3.fXmax - axis3.fXmin) * axis3.fNbins);
            if (bin1 < 0) bin1 = 0; else if (bin1 > axis1.fNbins + 1) bin1 = axis1.fNbins + 1;
            if (bin2 < 0) bin2 = 0; else if (bin2 > axis2.fNbins + 1) bin2 = axis2.fNbins + 1;
            if (bin3 < 0) bin3 = 0; else if (bin3 > axis3.fNbins + 1) bin3 = axis3.fNbins + 1;
            this.fArray[bin1 + (axis1.fNbins + 2) * (bin2 + (axis2.fNbins + 2) * bin3)] += ((weight === undefined) ? 1 : weight);
        }
    }

    if (typename.indexOf("TProfile") === 0) {
        if (typename.indexOf("TProfile2D") === 0) {
            m.getBin = function (x, y) {
                return (x + (this.fXaxis.fNbins + 2) * y);
            };
            m.getBinContent = function (x, y) {
                var bin = this.getBin(x, y);
                if (bin < 0 || bin >= this.fNcells) return 0;
                if (this.fBinEntries[bin] < 1e-300) return 0;
                if (!this.fArray) return 0;
                return this.fArray[bin] / this.fBinEntries[bin];
            };
            m.getBinEntries = function (x, y) {
                var bin = this.getBin(x, y);
                if (bin < 0 || bin >= this.fNcells) return 0;
                return this.fBinEntries[bin];
            }
        } else {
            m.getBin = function (x) {
                return x;
            };
            m.getBinContent = function (bin) {
                if (bin < 0 || bin >= this.fNcells) return 0;
                if (this.fBinEntries[bin] < 1e-300) return 0;
                if (!this.fArray) return 0;
                return this.fArray[bin] / this.fBinEntries[bin];
            };
        }
        m.getBinEffectiveEntries = function (bin) {
            if (bin < 0 || bin >= this.fNcells) return 0;
            var sumOfWeights = this.fBinEntries[bin];
            if (!this.fBinSumw2 || this.fBinSumw2.length !== this.fNcells) {
                // this can happen  when reading an old file
                return sumOfWeights;
            }
            var sumOfWeightsSquare = this.fBinSumw2[bin];
            return (sumOfWeightsSquare > 0) ? sumOfWeights * sumOfWeights / sumOfWeightsSquare : 0;
        };
        m.getBinError = function (bin) {
            if (bin < 0 || bin >= this.fNcells) return 0;
            var cont = this.fArray[bin],               // sum of bin w *y
                sum = this.fBinEntries[bin],          // sum of bin weights
                err2 = this.fSumw2[bin],               // sum of bin w * y^2
                neff = this.getBinEffectiveEntries(bin);  // (sum of w)^2 / (sum of w^2)
            if (sum < 1e-300) return 0;                  // for empty bins
            var EErrorType = {kERRORMEAN: 0, kERRORSPREAD: 1, kERRORSPREADI: 2, kERRORSPREADG: 3};
            // case the values y are gaussian distributed y +/- sigma and w = 1/sigma^2
            if (this.fErrorMode === EErrorType.kERRORSPREADG)
                return 1.0 / Math.sqrt(sum);
            // compute variance in y (eprim2) and standard deviation in y (eprim)
            var contsum = cont / sum, eprim = Math.sqrt(Math.abs(err2 / sum - contsum * contsum));
            if (this.fErrorMode === EErrorType.kERRORSPREADI) {
                if (eprim !== 0) return eprim / Math.sqrt(neff);
                // in case content y is an integer (so each my has an error +/- 1/sqrt(12)
                // when the std(y) is zero
                return 1.0 / Math.sqrt(12 * neff);
            }
            // if approximate compute the sums (of w, wy and wy2) using all the bins
            //  when the variance in y is zero
            // case option "S" return standard deviation in y
            if (this.fErrorMode === EErrorType.kERRORSPREAD) return eprim;
            // default case : fErrorMode = kERRORMEAN
            // return standard error on the mean of y
            return (eprim / Math.sqrt(neff));
        };
    }

    if (typename === "TAxis") {
        m.GetBinLowEdge = function (bin) {
            if (this.fNbins <= 0) return 0;
            if ((this.fXbins.length > 0) && (bin > 0) && (bin <= this.fNbins)) return this.fXbins[bin - 1];
            return this.fXmin + (bin - 1) * (this.fXmax - this.fXmin) / this.fNbins;
        };
        m.GetBinCenter = function (bin) {
            if (this.fNbins <= 0) return 0;
            if ((this.fXbins.length > 0) && (bin > 0) && (bin < this.fNbins)) return (this.fXbins[bin - 1] + this.fXbins[bin]) / 2;
            return this.fXmin + (bin - 0.5) * (this.fXmax - this.fXmin) / this.fNbins;
        }
    }

    if (typeof getMoreMethods == "function")
        getMoreMethods(m, typename, obj);

    methodsCache[typename] = m;
    return m;
}


/** @summary Adds specific methods to the object.
 *
 * JSROOT implements some basic methods for different ROOT classes.
 * @param {object} obj - object where methods are assigned
 * @param {string} typename - optional typename, if not specified, obj._typename will be used
 * @private
 */
function addMethods(obj, typename) {
    this.extend(obj, getMethods(typename || obj._typename, obj));
}
