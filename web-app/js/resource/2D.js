/*****
*
*   Array.js
*
*   copyright 2002, Kevin Lindsey
*
*   Add additional methods to the built-in Array object
*
*****/

/*****
*
*   foreach
*
*****/
Array.prototype.foreach = function(func) {
    for ( var i = 0; i < this.length; i++ )
        func(this[i]);
};


/*****
*
*   map
*
*****/
Array.prototype.map = function(func) {
    var result = new Array();

    for ( var i = 0; i < this.length; i++ )
        result.push( func(this[i]) );

    return result;
};


/*****
*
*   min
*
*****/
Array.prototype.min = function() {
    var min = this[0];

    for ( var i = 0; i < this.length; i++ )
        if ( this[i] < min ) min = this[i];

    return min;
}


/*****
*
*   max
*
*****/
Array.prototype.max = function() {
    var max = this[0];

    for ( var i = 0; i < this.length; i++ )
        if ( this[i] > max ) max = this[i];

    return max;
}
/*****
*
*   AntiZoomAndPan.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

AntiZoomAndPan.VERSION = "1.0"

/*****
*
*   constructor
*
*****/
function AntiZoomAndPan() {
    this.init();
}


/*****
*
*   init
*
*****/
AntiZoomAndPan.prototype.init = function() {
    var svgRoot = svgDocument.documentElement;

    // Initialize properties;
    this.svgNodes = new Array();
    this.x_trans  = 0;
    this.y_trans  = 0;
    this.scale    = 1;

    // Setup event listeners to capture zoom and scroll events
    svgRoot.addEventListener('SVGZoom',   this, false);
    svgRoot.addEventListener('SVGScroll', this, false);
    svgRoot.addEventListener('SVGResize', this, false);
};


/*****
*
*   appendNode
*
*****/
AntiZoomAndPan.prototype.appendNode = function(svgNode) {
    // Add node to a array
    this.svgNodes.push(svgNode);
};


/*****
*
*   removeNode
*
*****/
AntiZoomAndPan.prototype.removeNode = function(svgNode) {
    // Remove node if found
    for ( var i = 0; i < this.svgNodes.length; i++ ) {
        if ( this.svgNodes[i] === svgNode ) {
            this.svgNodes.splice(i, 1);
            break;
        }
    }
};


/*****
*
*   Event Handlers
*
*****/

/*****
*
*   handleEvent
*
*****/
AntiZoomAndPan.prototype.handleEvent = function(e) {
    var type = e.type;

    if ( this[type] == null )
        throw new Error("Unsupported event type: " + type);

    this[type](e);
};


/*****
*
*   SVGZoom
*
*****/
AntiZoomAndPan.prototype.SVGZoom = function(e) {
    // Update current transform
    this.update();
};


/*****
*
*   SVGScroll
*
*****/
AntiZoomAndPan.prototype.SVGScroll = function(e) {
    // Update current transform
    this.update();
};


/*****
*
*   SVGResize
*
*****/
AntiZoomAndPan.prototype.SVGResize = function(e) {
    // Update current transform
    this.update();
};


/*****
*
*   update
*
*****/
AntiZoomAndPan.prototype.update = function() {
    if ( this.svgNodes.length > 0 ) {
        var svgRoot = svgDocument.documentElement;
        var viewbox = new ViewBox(svgRoot);
        var matrix  = viewbox.getTM();
        var trans   = svgRoot.currentTranslate;

        // Set the scale factor to leave object at original size
        matrix = matrix.scale( 1.0 / svgRoot.currentScale);

        // Get the current pan settings
        matrix = matrix.translate(-trans.x, -trans.y);

        var transform = "matrix(" +
            [ matrix.a, matrix.b, matrix.c,
              matrix.d, matrix.e, matrix.f  ].join(",") + ")";

        // Apply combined transforms to all managed nodes
        for ( var i = 0; i < this.svgNodes.length; i++ ) {
            this.svgNodes[i].setAttributeNS(null, "transform", transform);
        }
    }
};

/*****
*
*   EventHandler.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function EventHandler() {
    this.init();
};


/*****
*
*   init
*
*****/
EventHandler.prototype.init = function() {
    // abstract method
};


/*****
*
*   handleEvent
*
*****/
EventHandler.prototype.handleEvent = function(e) {
    var type = e.type;

    if ( this[type] == null )
        throw new Error("Unsupported event type: " + type);

    this[type](e);
};

/*****
*
*   Mouser.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

var svgns = "http://www.w3.org/2000/svg";


/*****
*
*   Setup inheritance
*
*****/
Mouser.prototype             = new EventHandler();
Mouser.prototype.constructor = Mouser;
Mouser.superclass            = EventHandler.prototype;


/*****
*
*   constructor
*
*****/
function Mouser() {
    this.init();
}


/*****
*
*   init
*
*****/
Mouser.prototype.init = function() {
    // init properties
    this.svgNode     = null;
    this.handles     = new Array();
    this.shapes      = new Array();
    this.lastPoint   = null;
    this.currentNode = null;

    // build the mouse event region
    this.realize();
};


/*****
*
*   realize
*
*   Create rectangle to capture events.  This method has been separated from
*   the init() method to allow possible sub-classes to redefine the mouse
*   tracking shape
*
*****/
Mouser.prototype.realize = function() {
    // Make sure we don't create the event region twice
    if ( this.svgNode == null) {
        var rect = svgDocument.createElementNS(svgns, "rect");
        
        this.svgNode = rect;
        rect.setAttributeNS(null, "width", "100%");
        rect.setAttributeNS(null, "height", "100%");
        rect.setAttributeNS(null, "opacity", "0.0");
        rect.setAttributeNS(null, "display", "none");

        svgDocument.documentElement.appendChild(rect);
    }
};


/*****
*
*   selection management
*
*****/

/*****
*
*   register
*
*****/
Mouser.prototype.register = function(handle) {
    // See if this handler is current selection
    if ( this.handleIndex(handle) == -1 ) {
        var owner = handle.owner;

        // Show as selected
        handle.select(true);
        
        // Add it to the current selection
        this.handles.push(handle);

        if ( owner != null && this.shapeIndex(owner) == -1 )
            this.shapes.push(owner);
    }
};


/*****
*
*   unregister
*
*****/
Mouser.prototype.unregister = function(handle) {
    var index = this.handleIndex(handle);

    // Is this handler in the current selection?
    if ( index != -1 ) {
        // Deselect
        handle.select(false);
        
        // Remove from selection
        this.handles.splice(index, 1);
    }
};


/*****
*
*   registerShape
*
*****/
Mouser.prototype.registerShape = function(shape) {
    // See if this handler is current selection
    if ( this.shapeIndex(shape) == -1 ) {
        shape.select(true);

        // Add it to the current selection
        this.shapes.push(shape);
    }
};


/*****
*
*   unregisterShape
*
*****/
Mouser.prototype.unregisterShape = function(shape) {
    var index = this.shapeIndex(shape);

    if ( index != -1 ) {
        // Deselect
        shape.select(false);
        shape.selectHandles(false);
        shape.showHandles(false);
        shape.unregisterHandles();

        // Remove from list
        this.shapes.splice(index, 1);
    }
};


/*****
*
*   unregisterAll
*
*****/
Mouser.prototype.unregisterAll = function() {
    for ( var i = 0; i < this.handles.length; i++ ) {
        this.handles[i].select(false);
    }

    this.handles = new Array();
};


/*****
*
*   unregisterShapes
*
*****/
Mouser.prototype.unregisterShapes = function() {
    for ( var i = 0; i < this.shapes.length; i++ ) {
        var shape = this.shapes[i];
        
        shape.select(false);
        shape.selectHandles(false);
        shape.showHandles(false);
        shape.unregisterHandles();
    }

    this.shapes = new Array();
};


/*****
*
*   handleIndex
*
*****/
Mouser.prototype.handleIndex = function(handle) {
    var result = -1;

    for ( var i = 0; i < this.handles.length; i++ ) {
        if ( this.handles[i] === handle ) {
            result = i;
            break;
        }
    }

    return result;
};


/*****
*
*   shapeIndex
*
*****/
Mouser.prototype.shapeIndex = function(shape) {
    var result = -1;

    for ( var i = 0; i < this.shapes.length; i++ ) {
        if ( this.shapes[i] === shape ) {
            result = i;
            break;
        }
    }

    return result;
};


/*****
*
*   event handlers
*
*****/

/*****
*
*   beginDrag
*
*****/
Mouser.prototype.beginDrag = function(e) {
    this.currentNode = e.target;
    
    var svgPoint = this.getUserCoordinate( this.currentNode, e.clientX, e.clientY );
    
    this.lastPoint = new Point2D(svgPoint.x, svgPoint.y);
    this.svgNode.addEventListener("mouseup",   this, false);
    this.svgNode.addEventListener("mousemove", this, false);

    // Assure mouser is top-most element in SVG document
    svgDocument.documentElement.appendChild(this.svgNode);

    // Enable rectangle to capture events
    this.svgNode.setAttributeNS(null, "display", "inline");
};


/*****
*
*   mouseup
*
*****/
Mouser.prototype.mouseup = function(e) {
    this.lastPoint   = null;
    this.currentNode = null;

    this.svgNode.removeEventListener("mouseup",   this, false);
    this.svgNode.removeEventListener("mousemove", this, false);

    this.svgNode.setAttributeNS(null, "display", "none");
};


/*****
*
*   mousemove
*
*****/
Mouser.prototype.mousemove = function(e) {
    var svgPoint = this.getUserCoordinate( this.currentNode, e.clientX, e.clientY );
    var newPoint = new Point2D(svgPoint.x, svgPoint.y);
    var delta    = newPoint.subtract(this.lastPoint);
    var updates  = new Array();
    var updateId = new Date().getTime();

    this.lastPoint.setFromPoint(newPoint);

    for ( var i = 0; i < this.handles.length; i++ ) {
        var handle = this.handles[i];
        var owner  = handle.owner;
        
        handle.translate(delta);
        if ( owner != null ) {
            if ( owner.lastUpdate != updateId ) {
                owner.lastUpdate = updateId;
                updates.push(owner);
            }
        } else {
            updates.push(handle);
        }
    }

    // perform updates
    for ( var i = 0; i < updates.length; i++ ) {
        updates[i].update();
    }
};


/*****
*
*   getUserCoordinate
*
*****/
Mouser.prototype.getUserCoordinate = function(node, x, y) {
    var svgRoot    = svgDocument.documentElement;
    var pan        = svgRoot.getCurrentTranslate();
    var zoom       = svgRoot.getCurrentScale();
    var CTM        = this.getTransformToElement(node);
    var iCTM       = CTM.inverse();
    var worldPoint = svgDocument.documentElement.createSVGPoint();
    
    worldPoint.x = (x - pan.x) / zoom;
    worldPoint.y = (y - pan.y) / zoom;

    return worldPoint.matrixTransform(iCTM);
};


/*****
*
*   getTransformToElement
*
*****/
Mouser.prototype.getTransformToElement = function(node) {
    var CTM = node.getCTM();

    while ( (node = node.parentNode) != svgDocument ) {
        CTM = node.getCTM().multiply(CTM);
    }
    
    return CTM;
};
/*****
*
*   ViewBox.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

ViewBox.VERSION = "1.0";


/*****
*
*   constructor
*
*****/
function ViewBox(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
ViewBox.prototype.init = function(svgNode) {
    var viewBox = svgNode.getAttributeNS(null, "viewBox");
    var preserveAspectRatio = svgNode.getAttributeNS(null, "preserveAspectRatio");
    
    if ( viewBox != "" ) {
        var params = viewBox.split(/\s*,\s*|\s+/);

        this.x      = parseFloat( params[0] );
        this.y      = parseFloat( params[1] );
        this.width  = parseFloat( params[2] );
        this.height = parseFloat( params[3] );
    } else {
        // NOTE: Need to put an SVGResize event handler on the svgNode to keep
        // these values in sync with the window size or should add additional
        // logic (probably a flag) to getTM() so it will know to use the window
        // dimensions instead of this object's width and height properties
        this.x      = 0;
        this.y      = 0;
        this.width  = innerWidth;
        this.height = innerHeight;
    }
    
    this.setPAR(preserveAspectRatio);
};


/*****
*
*   getTM
*
*****/
ViewBox.prototype.getTM = function() {
    var svgRoot      = svgDocument.documentElement;
    var matrix       = svgDocument.documentElement.createSVGMatrix();
    var windowWidth  = svgRoot.getAttributeNS(null, "width");
    var windowHeight = svgRoot.getAttributeNS(null, "height");

    windowWidth  = ( windowWidth  != "" ) ? parseFloat(windowWidth)  : innerWidth;
    windowHeight = ( windowHeight != "" ) ? parseFloat(windowHeight) : innerHeight;

    var x_ratio = this.width  / windowWidth;
    var y_ratio = this.height / windowHeight;

    matrix = matrix.translate(this.x, this.y);
    if ( this.alignX == "none" ) {
        matrix = matrix.scaleNonUniform( x_ratio, y_ratio );
    } else {
        if ( x_ratio < y_ratio && this.meetOrSlice == "meet" ||
             x_ratio > y_ratio && this.meetOrSlice == "slice"   )
        {
            var x_trans = 0;
            var x_diff  = windowWidth*y_ratio - this.width;

            if ( this.alignX == "Mid" )
                x_trans = -x_diff/2;
            else if ( this.alignX == "Max" )
                x_trans = -x_diff;
            
            matrix = matrix.translate(x_trans, 0);
            matrix = matrix.scale( y_ratio );
        }
        else if ( x_ratio > y_ratio && this.meetOrSlice == "meet" ||
                  x_ratio < y_ratio && this.meetOrSlice == "slice"   )
        {
            var y_trans = 0;
            var y_diff  = windowHeight*x_ratio - this.height;

            if ( this.alignY == "Mid" )
                y_trans = -y_diff/2;
            else if ( this.alignY == "Max" )
                y_trans = -y_diff;
            
            matrix = matrix.translate(0, y_trans);
            matrix = matrix.scale( x_ratio );
        }
        else
        {
            // x_ratio == y_ratio so, there is no need to translate
            // We can scale by either value
            matrix = matrix.scale( x_ratio );
        }
    }

    return matrix;
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   setPAR
*
*****/
ViewBox.prototype.setPAR = function(PAR) {
    // NOTE: This function needs to use default values when encountering
    // unrecognized values
    if ( PAR ) {
        var params = PAR.split(/\s+/);
        var align  = params[0];

        if ( align == "none" ) {
            this.alignX = "none";
            this.alignY = "none";
        } else {
            this.alignX = align.substring(1,4);
            this.alignY = align.substring(5,9);
        }

        if ( params.length == 2 ) {
            this.meetOrSlice = params[1];
        } else {
            this.meetOrSlice = "meet";
        }
    } else {
        this.align  = "xMidYMid";
        this.alignX = "Mid";
        this.alignY = "Mid";
        this.meetOrSlice = "meet";
    }
};

/*****
*
*   Intersection.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function Intersection(status) {
    if ( arguments.length > 0 ) {
        this.init(status);
    }
}


/*****
*
*   init
*
*****/
Intersection.prototype.init = function(status) {
    this.status = status;
    this.points = new Array();
};


/*****
*
*   appendPoint
*
*****/
Intersection.prototype.appendPoint = function(point) {
    this.points.push(point);
};


/*****
*
*   appendPoints
*
*****/
Intersection.prototype.appendPoints = function(points) {
    this.points = this.points.concat(points);
};


/*****
*
*   class methods
*
*****/

/*****
*
*   intersectShapes
*
*****/
Intersection.intersectShapes = function(shape1, shape2) {
    var ip1 = shape1.getIntersectionParams();
    var ip2 = shape2.getIntersectionParams();
    var result;

    if ( ip1 != null && ip2 != null ) {
        if ( ip1.name == "Path" ) {
            result = Intersection.intersectPathShape(shape1, shape2);
        } else if ( ip2.name == "Path" ) {
            result = Intersection.intersectPathShape(shape2, shape1);
        } else {
            var method;
            var params;

            if ( ip1.name < ip2.name ) {
                method = "intersect" + ip1.name + ip2.name;
                params = ip1.params.concat( ip2.params );
            } else {
                method = "intersect" + ip2.name + ip1.name;
                params = ip2.params.concat( ip1.params );
            }

            if ( !(method in Intersection) )
                throw new Error("Intersection not available: " + method);

            result = Intersection[method].apply(null, params);
        }
    } else {
        result = new Intersection("No Intersection");
    }

    return result;
};


/*****
*
*   intersectPathShape
*
*****/
Intersection.intersectPathShape = function(path, shape) {
    return path.intersectShape(shape);
};


/*****
*
*   intersectBezier2Bezier2
*
*****/
Intersection.intersectBezier2Bezier2 = function(a1, a2, a3, b1, b2, b3) {
    var a, b;
    var c12, c11, c10;
    var c22, c21, c20;
    var result = new Intersection("No Intersection");
    var poly;

    a = a2.multiply(-2);
    c12 = a1.add(a.add(a3));

    a = a1.multiply(-2);
    b = a2.multiply(2);
    c11 = a.add(b);

    c10 = new Point2D(a1.x, a1.y);

    a = b2.multiply(-2);
    c22 = b1.add(a.add(b3));

    a = b1.multiply(-2);
    b = b2.multiply(2);
    c21 = a.add(b);

    c20 = new Point2D(b1.x, b1.y);
    
    if ( c12.y == 0 ) {
        var v0 = c12.x*(c10.y - c20.y);
        var v1 = v0 - c11.x*c11.y;
        var v2 = v0 + v1;
        var v3 = c11.y*c11.y;

        poly = new Polynomial(
            c12.x*c22.y*c22.y,
            2*c12.x*c21.y*c22.y,
            c12.x*c21.y*c21.y - c22.x*v3 - c22.y*v0 - c22.y*v1,
            -c21.x*v3 - c21.y*v0 - c21.y*v1,
            (c10.x - c20.x)*v3 + (c10.y - c20.y)*v1
        );
    } else {
        var v0 = c12.x*c22.y - c12.y*c22.x;
        var v1 = c12.x*c21.y - c21.x*c12.y;
        var v2 = c11.x*c12.y - c11.y*c12.x;
        var v3 = c10.y - c20.y;
        var v4 = c12.y*(c10.x - c20.x) - c12.x*v3;
        var v5 = -c11.y*v2 + c12.y*v4;
        var v6 = v2*v2;

        poly = new Polynomial(
            v0*v0,
            2*v0*v1,
            (-c22.y*v6 + c12.y*v1*v1 + c12.y*v0*v4 + v0*v5) / c12.y,
            (-c21.y*v6 + c12.y*v1*v4 + v1*v5) / c12.y,
            (v3*v6 + v4*v5) / c12.y
        );
    }

    var roots = poly.getRoots();
    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];

        if ( 0 <= s && s <= 1 ) {
            var xRoots = new Polynomial(
                c12.x,
                c11.x,
                c10.x - c20.x - s*c21.x - s*s*c22.x
            ).getRoots();
            var yRoots = new Polynomial(
                c12.y,
                c11.y,
                c10.y - c20.y - s*c21.y - s*s*c22.y
            ).getRoots();

            if ( xRoots.length > 0 && yRoots.length > 0 ) {
                var TOLERANCE = 1e-4;

                checkRoots:
                for ( var j = 0; j < xRoots.length; j++ ) {
                    var xRoot = xRoots[j];

                    if ( 0 <= xRoot && xRoot <= 1 ) {
                        for ( var k = 0; k < yRoots.length; k++ ) {
                            if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                                result.points.push( c22.multiply(s*s).add(c21.multiply(s).add(c20)) );
                                break checkRoots;
                            }
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Bezier3
*
*****/
Intersection.intersectBezier2Bezier3 = function(a1, a2, a3, b1, b2, b3, b4) {
    var a, b,c, d;
    var c12, c11, c10;
    var c23, c22, c21, c20;
    var result = new Intersection("No Intersection");

    a = a2.multiply(-2);
    c12 = a1.add(a.add(a3));

    a = a1.multiply(-2);
    b = a2.multiply(2);
    c11 = a.add(b);

    c10 = new Point2D(a1.x, a1.y);

    a = b1.multiply(-1);
    b = b2.multiply(3);
    c = b3.multiply(-3);
    d = a.add(b.add(c.add(b4)));
    c23 = new Vector2D(d.x, d.y);

    a = b1.multiply(3);
    b = b2.multiply(-6);
    c = b3.multiply(3);
    d = a.add(b.add(c));
    c22 = new Vector2D(d.x, d.y);

    a = b1.multiply(-3);
    b = b2.multiply(3);
    c = a.add(b);
    c21 = new Vector2D(c.x, c.y);

    c20 = new Vector2D(b1.x, b1.y);

    var c10x2 = c10.x*c10.x;
    var c10y2 = c10.y*c10.y;
    var c11x2 = c11.x*c11.x;
    var c11y2 = c11.y*c11.y;
    var c12x2 = c12.x*c12.x;
    var c12y2 = c12.y*c12.y;
    var c20x2 = c20.x*c20.x;
    var c20y2 = c20.y*c20.y;
    var c21x2 = c21.x*c21.x;
    var c21y2 = c21.y*c21.y;
    var c22x2 = c22.x*c22.x;
    var c22y2 = c22.y*c22.y;
    var c23x2 = c23.x*c23.x;
    var c23y2 = c23.y*c23.y;

    var poly = new Polynomial(
        -2*c12.x*c12.y*c23.x*c23.y + c12x2*c23y2 + c12y2*c23x2,
        -2*c12.x*c12.y*c22.x*c23.y - 2*c12.x*c12.y*c22.y*c23.x + 2*c12y2*c22.x*c23.x +
            2*c12x2*c22.y*c23.y,
        -2*c12.x*c21.x*c12.y*c23.y - 2*c12.x*c12.y*c21.y*c23.x - 2*c12.x*c12.y*c22.x*c22.y +
            2*c21.x*c12y2*c23.x + c12y2*c22x2 + c12x2*(2*c21.y*c23.y + c22y2),
        2*c10.x*c12.x*c12.y*c23.y + 2*c10.y*c12.x*c12.y*c23.x + c11.x*c11.y*c12.x*c23.y +
            c11.x*c11.y*c12.y*c23.x - 2*c20.x*c12.x*c12.y*c23.y - 2*c12.x*c20.y*c12.y*c23.x -
            2*c12.x*c21.x*c12.y*c22.y - 2*c12.x*c12.y*c21.y*c22.x - 2*c10.x*c12y2*c23.x -
            2*c10.y*c12x2*c23.y + 2*c20.x*c12y2*c23.x + 2*c21.x*c12y2*c22.x -
            c11y2*c12.x*c23.x - c11x2*c12.y*c23.y + c12x2*(2*c20.y*c23.y + 2*c21.y*c22.y),
        2*c10.x*c12.x*c12.y*c22.y + 2*c10.y*c12.x*c12.y*c22.x + c11.x*c11.y*c12.x*c22.y +
            c11.x*c11.y*c12.y*c22.x - 2*c20.x*c12.x*c12.y*c22.y - 2*c12.x*c20.y*c12.y*c22.x -
            2*c12.x*c21.x*c12.y*c21.y - 2*c10.x*c12y2*c22.x - 2*c10.y*c12x2*c22.y +
            2*c20.x*c12y2*c22.x - c11y2*c12.x*c22.x - c11x2*c12.y*c22.y + c21x2*c12y2 +
            c12x2*(2*c20.y*c22.y + c21y2),
        2*c10.x*c12.x*c12.y*c21.y + 2*c10.y*c12.x*c21.x*c12.y + c11.x*c11.y*c12.x*c21.y +
            c11.x*c11.y*c21.x*c12.y - 2*c20.x*c12.x*c12.y*c21.y - 2*c12.x*c20.y*c21.x*c12.y -
            2*c10.x*c21.x*c12y2 - 2*c10.y*c12x2*c21.y + 2*c20.x*c21.x*c12y2 -
            c11y2*c12.x*c21.x - c11x2*c12.y*c21.y + 2*c12x2*c20.y*c21.y,
        -2*c10.x*c10.y*c12.x*c12.y - c10.x*c11.x*c11.y*c12.y - c10.y*c11.x*c11.y*c12.x +
            2*c10.x*c12.x*c20.y*c12.y + 2*c10.y*c20.x*c12.x*c12.y + c11.x*c20.x*c11.y*c12.y +
            c11.x*c11.y*c12.x*c20.y - 2*c20.x*c12.x*c20.y*c12.y - 2*c10.x*c20.x*c12y2 +
            c10.x*c11y2*c12.x + c10.y*c11x2*c12.y - 2*c10.y*c12x2*c20.y -
            c20.x*c11y2*c12.x - c11x2*c20.y*c12.y + c10x2*c12y2 + c10y2*c12x2 +
            c20x2*c12y2 + c12x2*c20y2
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];
        var xRoots = new Polynomial(
            c12.x,
            c11.x,
            c10.x - c20.x - s*c21.x - s*s*c22.x - s*s*s*c23.x
        ).getRoots();
        var yRoots = new Polynomial(
            c12.y,
            c11.y,
            c10.y - c20.y - s*c21.y - s*s*c22.y - s*s*s*c23.y
        ).getRoots();

        if ( xRoots.length > 0 && yRoots.length > 0 ) {
            var TOLERANCE = 1e-4;

            checkRoots:
            for ( var j = 0; j < xRoots.length; j++ ) {
                var xRoot = xRoots[j];
                
                if ( 0 <= xRoot && xRoot <= 1 ) {
                    for ( var k = 0; k < yRoots.length; k++ ) {
                        if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                            result.points.push(
                                c23.multiply(s*s*s).add(c22.multiply(s*s).add(c21.multiply(s).add(c20)))
                            );
                            break checkRoots;
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;

};


/*****
*
*   intersectBezier2Circle
*
*****/
Intersection.intersectBezier2Circle = function(p1, p2, p3, c, r) {
    return Intersection.intersectBezier2Ellipse(p1, p2, p3, c, r, r);
};


/*****
*
*   intersectBezier2Ellipse
*
*****/
Intersection.intersectBezier2Ellipse = function(p1, p2, p3, ec, rx, ry) {
    var a, b;       // temporary variables
    var c2, c1, c0; // coefficients of quadratic
    var result = new Intersection("No Intersection");

    a = p2.multiply(-2);
    c2 = p1.add(a.add(p3));

    a = p1.multiply(-2);
    b = p2.multiply(2);
    c1 = a.add(b);

    c0 = new Point2D(p1.x, p1.y);

    var rxrx  = rx*rx;
    var ryry  = ry*ry;
    var roots = new Polynomial(
        ryry*c2.x*c2.x + rxrx*c2.y*c2.y,
        2*(ryry*c2.x*c1.x + rxrx*c2.y*c1.y),
        ryry*(2*c2.x*c0.x + c1.x*c1.x) + rxrx*(2*c2.y*c0.y+c1.y*c1.y) -
            2*(ryry*ec.x*c2.x + rxrx*ec.y*c2.y),
        2*(ryry*c1.x*(c0.x-ec.x) + rxrx*c1.y*(c0.y-ec.y)),
        ryry*(c0.x*c0.x+ec.x*ec.x) + rxrx*(c0.y*c0.y + ec.y*ec.y) -
            2*(ryry*ec.x*c0.x + rxrx*ec.y*c0.y) - rxrx*ryry
    ).getRoots();

    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 )
            result.points.push( c2.multiply(t*t).add(c1.multiply(t).add(c0)) );
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Line
*
*****/
Intersection.intersectBezier2Line = function(p1, p2, p3, a1, a2) {
    var a, b;             // temporary variables
    var c2, c1, c0;       // coefficients of quadratic
    var cl;               // c coefficient for normal form of line
    var n;                // normal for normal form of line
    var min = a1.min(a2); // used to determine if point is on line segment
    var max = a1.max(a2); // used to determine if point is on line segment
    var result = new Intersection("No Intersection");
    
    a = p2.multiply(-2);
    c2 = p1.add(a.add(p3));

    a = p1.multiply(-2);
    b = p2.multiply(2);
    c1 = a.add(b);

    c0 = new Point2D(p1.x, p1.y);

    // Convert line to normal form: ax + by + c = 0
    // Find normal to line: negative inverse of original line's slope
    n = new Vector2D(a1.y - a2.y, a2.x - a1.x);
    
    // Determine new c coefficient
    cl = a1.x*a2.y - a2.x*a1.y;

    // Transform cubic coefficients to line's coordinate system and find roots
    // of cubic
    roots = new Polynomial(
        n.dot(c2),
        n.dot(c1),
        n.dot(c0) + cl
    ).getRoots();

    // Any roots in closed interval [0,1] are intersections on Bezier, but
    // might not be on the line segment.
    // Find intersections and calculate point coordinates
    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 ) {
            // We're within the Bezier curve
            // Find point on Bezier
            var p4 = p1.lerp(p2, t);
            var p5 = p2.lerp(p3, t);

            var p6 = p4.lerp(p5, t);

            // See if point is on line segment
            // Had to make special cases for vertical and horizontal lines due
            // to slight errors in calculation of p6
            if ( a1.x == a2.x ) {
                if ( min.y <= p6.y && p6.y <= max.y ) {
                    result.status = "Intersection";
                    result.appendPoint( p6 );
                }
            } else if ( a1.y == a2.y ) {
                if ( min.x <= p6.x && p6.x <= max.x ) {
                    result.status = "Intersection";
                    result.appendPoint( p6 );
                }
            } else if ( p6.gte(min) && p6.lte(max) ) {
                result.status = "Intersection";
                result.appendPoint( p6 );
            }
        }
    }

    return result;
};


/*****
*
*   intersectBezier2Polygon
*
*****/
Intersection.intersectBezier2Polygon = function(p1, p2, p3, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];
        var inter = Intersection.intersectBezier2Line(p1, p2, p3, a1, a2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier2Rectangle
*
*****/
Intersection.intersectBezier2Rectangle = function(p1, p2, p3, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectBezier2Line(p1, p2, p3, min, topRight);
    var inter2 = Intersection.intersectBezier2Line(p1, p2, p3, topRight, max);
    var inter3 = Intersection.intersectBezier2Line(p1, p2, p3, max, bottomLeft);
    var inter4 = Intersection.intersectBezier2Line(p1, p2, p3, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Bezier3
*
*****/
Intersection.intersectBezier3Bezier3 = function(a1, a2, a3, a4, b1, b2, b3, b4) {
    var a, b, c, d;         // temporary variables
    var c13, c12, c11, c10; // coefficients of cubic
    var c23, c22, c21, c20; // coefficients of cubic
    var result = new Intersection("No Intersection");

    // Calculate the coefficients of cubic polynomial
    a = a1.multiply(-1);
    b = a2.multiply(3);
    c = a3.multiply(-3);
    d = a.add(b.add(c.add(a4)));
    c13 = new Vector2D(d.x, d.y);

    a = a1.multiply(3);
    b = a2.multiply(-6);
    c = a3.multiply(3);
    d = a.add(b.add(c));
    c12 = new Vector2D(d.x, d.y);

    a = a1.multiply(-3);
    b = a2.multiply(3);
    c = a.add(b);
    c11 = new Vector2D(c.x, c.y);

    c10 = new Vector2D(a1.x, a1.y);

    a = b1.multiply(-1);
    b = b2.multiply(3);
    c = b3.multiply(-3);
    d = a.add(b.add(c.add(b4)));
    c23 = new Vector2D(d.x, d.y);

    a = b1.multiply(3);
    b = b2.multiply(-6);
    c = b3.multiply(3);
    d = a.add(b.add(c));
    c22 = new Vector2D(d.x, d.y);

    a = b1.multiply(-3);
    b = b2.multiply(3);
    c = a.add(b);
    c21 = new Vector2D(c.x, c.y);

    c20 = new Vector2D(b1.x, b1.y);

    var c10x2 = c10.x*c10.x;
    var c10x3 = c10.x*c10.x*c10.x;
    var c10y2 = c10.y*c10.y;
    var c10y3 = c10.y*c10.y*c10.y;
    var c11x2 = c11.x*c11.x;
    var c11x3 = c11.x*c11.x*c11.x;
    var c11y2 = c11.y*c11.y;
    var c11y3 = c11.y*c11.y*c11.y;
    var c12x2 = c12.x*c12.x;
    var c12x3 = c12.x*c12.x*c12.x;
    var c12y2 = c12.y*c12.y;
    var c12y3 = c12.y*c12.y*c12.y;
    var c13x2 = c13.x*c13.x;
    var c13x3 = c13.x*c13.x*c13.x;
    var c13y2 = c13.y*c13.y;
    var c13y3 = c13.y*c13.y*c13.y;
    var c20x2 = c20.x*c20.x;
    var c20x3 = c20.x*c20.x*c20.x;
    var c20y2 = c20.y*c20.y;
    var c20y3 = c20.y*c20.y*c20.y;
    var c21x2 = c21.x*c21.x;
    var c21x3 = c21.x*c21.x*c21.x;
    var c21y2 = c21.y*c21.y;
    var c22x2 = c22.x*c22.x;
    var c22x3 = c22.x*c22.x*c22.x;
    var c22y2 = c22.y*c22.y;
    var c23x2 = c23.x*c23.x;
    var c23x3 = c23.x*c23.x*c23.x;
    var c23y2 = c23.y*c23.y;
    var c23y3 = c23.y*c23.y*c23.y;
    var poly = new Polynomial(
        -c13x3*c23y3 + c13y3*c23x3 - 3*c13.x*c13y2*c23x2*c23.y +
            3*c13x2*c13.y*c23.x*c23y2,
        -6*c13.x*c22.x*c13y2*c23.x*c23.y + 6*c13x2*c13.y*c22.y*c23.x*c23.y + 3*c22.x*c13y3*c23x2 -
            3*c13x3*c22.y*c23y2 - 3*c13.x*c13y2*c22.y*c23x2 + 3*c13x2*c22.x*c13.y*c23y2,
        -6*c21.x*c13.x*c13y2*c23.x*c23.y - 6*c13.x*c22.x*c13y2*c22.y*c23.x + 6*c13x2*c22.x*c13.y*c22.y*c23.y +
            3*c21.x*c13y3*c23x2 + 3*c22x2*c13y3*c23.x + 3*c21.x*c13x2*c13.y*c23y2 - 3*c13.x*c21.y*c13y2*c23x2 -
            3*c13.x*c22x2*c13y2*c23.y + c13x2*c13.y*c23.x*(6*c21.y*c23.y + 3*c22y2) + c13x3*(-c21.y*c23y2 -
            2*c22y2*c23.y - c23.y*(2*c21.y*c23.y + c22y2)),
        c11.x*c12.y*c13.x*c13.y*c23.x*c23.y - c11.y*c12.x*c13.x*c13.y*c23.x*c23.y + 6*c21.x*c22.x*c13y3*c23.x +
            3*c11.x*c12.x*c13.x*c13.y*c23y2 + 6*c10.x*c13.x*c13y2*c23.x*c23.y - 3*c11.x*c12.x*c13y2*c23.x*c23.y -
            3*c11.y*c12.y*c13.x*c13.y*c23x2 - 6*c10.y*c13x2*c13.y*c23.x*c23.y - 6*c20.x*c13.x*c13y2*c23.x*c23.y +
            3*c11.y*c12.y*c13x2*c23.x*c23.y - 2*c12.x*c12y2*c13.x*c23.x*c23.y - 6*c21.x*c13.x*c22.x*c13y2*c23.y -
            6*c21.x*c13.x*c13y2*c22.y*c23.x - 6*c13.x*c21.y*c22.x*c13y2*c23.x + 6*c21.x*c13x2*c13.y*c22.y*c23.y +
            2*c12x2*c12.y*c13.y*c23.x*c23.y + c22x3*c13y3 - 3*c10.x*c13y3*c23x2 + 3*c10.y*c13x3*c23y2 +
            3*c20.x*c13y3*c23x2 + c12y3*c13.x*c23x2 - c12x3*c13.y*c23y2 - 3*c10.x*c13x2*c13.y*c23y2 +
            3*c10.y*c13.x*c13y2*c23x2 - 2*c11.x*c12.y*c13x2*c23y2 + c11.x*c12.y*c13y2*c23x2 - c11.y*c12.x*c13x2*c23y2 +
            2*c11.y*c12.x*c13y2*c23x2 + 3*c20.x*c13x2*c13.y*c23y2 - c12.x*c12y2*c13.y*c23x2 -
            3*c20.y*c13.x*c13y2*c23x2 + c12x2*c12.y*c13.x*c23y2 - 3*c13.x*c22x2*c13y2*c22.y +
            c13x2*c13.y*c23.x*(6*c20.y*c23.y + 6*c21.y*c22.y) + c13x2*c22.x*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c13x3*(-2*c21.y*c22.y*c23.y - c20.y*c23y2 - c22.y*(2*c21.y*c23.y + c22y2) - c23.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        6*c11.x*c12.x*c13.x*c13.y*c22.y*c23.y + c11.x*c12.y*c13.x*c22.x*c13.y*c23.y + c11.x*c12.y*c13.x*c13.y*c22.y*c23.x -
            c11.y*c12.x*c13.x*c22.x*c13.y*c23.y - c11.y*c12.x*c13.x*c13.y*c22.y*c23.x - 6*c11.y*c12.y*c13.x*c22.x*c13.y*c23.x -
            6*c10.x*c22.x*c13y3*c23.x + 6*c20.x*c22.x*c13y3*c23.x + 6*c10.y*c13x3*c22.y*c23.y + 2*c12y3*c13.x*c22.x*c23.x -
            2*c12x3*c13.y*c22.y*c23.y + 6*c10.x*c13.x*c22.x*c13y2*c23.y + 6*c10.x*c13.x*c13y2*c22.y*c23.x +
            6*c10.y*c13.x*c22.x*c13y2*c23.x - 3*c11.x*c12.x*c22.x*c13y2*c23.y - 3*c11.x*c12.x*c13y2*c22.y*c23.x +
            2*c11.x*c12.y*c22.x*c13y2*c23.x + 4*c11.y*c12.x*c22.x*c13y2*c23.x - 6*c10.x*c13x2*c13.y*c22.y*c23.y -
            6*c10.y*c13x2*c22.x*c13.y*c23.y - 6*c10.y*c13x2*c13.y*c22.y*c23.x - 4*c11.x*c12.y*c13x2*c22.y*c23.y -
            6*c20.x*c13.x*c22.x*c13y2*c23.y - 6*c20.x*c13.x*c13y2*c22.y*c23.x - 2*c11.y*c12.x*c13x2*c22.y*c23.y +
            3*c11.y*c12.y*c13x2*c22.x*c23.y + 3*c11.y*c12.y*c13x2*c22.y*c23.x - 2*c12.x*c12y2*c13.x*c22.x*c23.y -
            2*c12.x*c12y2*c13.x*c22.y*c23.x - 2*c12.x*c12y2*c22.x*c13.y*c23.x - 6*c20.y*c13.x*c22.x*c13y2*c23.x -
            6*c21.x*c13.x*c21.y*c13y2*c23.x - 6*c21.x*c13.x*c22.x*c13y2*c22.y + 6*c20.x*c13x2*c13.y*c22.y*c23.y +
            2*c12x2*c12.y*c13.x*c22.y*c23.y + 2*c12x2*c12.y*c22.x*c13.y*c23.y + 2*c12x2*c12.y*c13.y*c22.y*c23.x +
            3*c21.x*c22x2*c13y3 + 3*c21x2*c13y3*c23.x - 3*c13.x*c21.y*c22x2*c13y2 - 3*c21x2*c13.x*c13y2*c23.y +
            c13x2*c22.x*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c13x2*c13.y*c23.x*(6*c20.y*c22.y + 3*c21y2) +
            c21.x*c13x2*c13.y*(6*c21.y*c23.y + 3*c22y2) + c13x3*(-2*c20.y*c22.y*c23.y - c23.y*(2*c20.y*c22.y + c21y2) -
            c21.y*(2*c21.y*c23.y + c22y2) - c22.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        c11.x*c21.x*c12.y*c13.x*c13.y*c23.y + c11.x*c12.y*c13.x*c21.y*c13.y*c23.x + c11.x*c12.y*c13.x*c22.x*c13.y*c22.y -
            c11.y*c12.x*c21.x*c13.x*c13.y*c23.y - c11.y*c12.x*c13.x*c21.y*c13.y*c23.x - c11.y*c12.x*c13.x*c22.x*c13.y*c22.y -
            6*c11.y*c21.x*c12.y*c13.x*c13.y*c23.x - 6*c10.x*c21.x*c13y3*c23.x + 6*c20.x*c21.x*c13y3*c23.x +
            2*c21.x*c12y3*c13.x*c23.x + 6*c10.x*c21.x*c13.x*c13y2*c23.y + 6*c10.x*c13.x*c21.y*c13y2*c23.x +
            6*c10.x*c13.x*c22.x*c13y2*c22.y + 6*c10.y*c21.x*c13.x*c13y2*c23.x - 3*c11.x*c12.x*c21.x*c13y2*c23.y -
            3*c11.x*c12.x*c21.y*c13y2*c23.x - 3*c11.x*c12.x*c22.x*c13y2*c22.y + 2*c11.x*c21.x*c12.y*c13y2*c23.x +
            4*c11.y*c12.x*c21.x*c13y2*c23.x - 6*c10.y*c21.x*c13x2*c13.y*c23.y - 6*c10.y*c13x2*c21.y*c13.y*c23.x -
            6*c10.y*c13x2*c22.x*c13.y*c22.y - 6*c20.x*c21.x*c13.x*c13y2*c23.y - 6*c20.x*c13.x*c21.y*c13y2*c23.x -
            6*c20.x*c13.x*c22.x*c13y2*c22.y + 3*c11.y*c21.x*c12.y*c13x2*c23.y - 3*c11.y*c12.y*c13.x*c22x2*c13.y +
            3*c11.y*c12.y*c13x2*c21.y*c23.x + 3*c11.y*c12.y*c13x2*c22.x*c22.y - 2*c12.x*c21.x*c12y2*c13.x*c23.y -
            2*c12.x*c21.x*c12y2*c13.y*c23.x - 2*c12.x*c12y2*c13.x*c21.y*c23.x - 2*c12.x*c12y2*c13.x*c22.x*c22.y -
            6*c20.y*c21.x*c13.x*c13y2*c23.x - 6*c21.x*c13.x*c21.y*c22.x*c13y2 + 6*c20.y*c13x2*c21.y*c13.y*c23.x +
            2*c12x2*c21.x*c12.y*c13.y*c23.y + 2*c12x2*c12.y*c21.y*c13.y*c23.x + 2*c12x2*c12.y*c22.x*c13.y*c22.y -
            3*c10.x*c22x2*c13y3 + 3*c20.x*c22x2*c13y3 + 3*c21x2*c22.x*c13y3 + c12y3*c13.x*c22x2 +
            3*c10.y*c13.x*c22x2*c13y2 + c11.x*c12.y*c22x2*c13y2 + 2*c11.y*c12.x*c22x2*c13y2 -
            c12.x*c12y2*c22x2*c13.y - 3*c20.y*c13.x*c22x2*c13y2 - 3*c21x2*c13.x*c13y2*c22.y +
            c12x2*c12.y*c13.x*(2*c21.y*c23.y + c22y2) + c11.x*c12.x*c13.x*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c21.x*c13x2*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c12x3*c13.y*(-2*c21.y*c23.y - c22y2) +
            c10.y*c13x3*(6*c21.y*c23.y + 3*c22y2) + c11.y*c12.x*c13x2*(-2*c21.y*c23.y - c22y2) +
            c11.x*c12.y*c13x2*(-4*c21.y*c23.y - 2*c22y2) + c10.x*c13x2*c13.y*(-6*c21.y*c23.y - 3*c22y2) +
            c13x2*c22.x*c13.y*(6*c20.y*c22.y + 3*c21y2) + c20.x*c13x2*c13.y*(6*c21.y*c23.y + 3*c22y2) +
            c13x3*(-2*c20.y*c21.y*c23.y - c22.y*(2*c20.y*c22.y + c21y2) - c20.y*(2*c21.y*c23.y + c22y2) -
            c21.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        -c10.x*c11.x*c12.y*c13.x*c13.y*c23.y + c10.x*c11.y*c12.x*c13.x*c13.y*c23.y + 6*c10.x*c11.y*c12.y*c13.x*c13.y*c23.x -
            6*c10.y*c11.x*c12.x*c13.x*c13.y*c23.y - c10.y*c11.x*c12.y*c13.x*c13.y*c23.x + c10.y*c11.y*c12.x*c13.x*c13.y*c23.x +
            c11.x*c11.y*c12.x*c12.y*c13.x*c23.y - c11.x*c11.y*c12.x*c12.y*c13.y*c23.x + c11.x*c20.x*c12.y*c13.x*c13.y*c23.y +
            c11.x*c20.y*c12.y*c13.x*c13.y*c23.x + c11.x*c21.x*c12.y*c13.x*c13.y*c22.y + c11.x*c12.y*c13.x*c21.y*c22.x*c13.y -
            c20.x*c11.y*c12.x*c13.x*c13.y*c23.y - 6*c20.x*c11.y*c12.y*c13.x*c13.y*c23.x - c11.y*c12.x*c20.y*c13.x*c13.y*c23.x -
            c11.y*c12.x*c21.x*c13.x*c13.y*c22.y - c11.y*c12.x*c13.x*c21.y*c22.x*c13.y - 6*c11.y*c21.x*c12.y*c13.x*c22.x*c13.y -
            6*c10.x*c20.x*c13y3*c23.x - 6*c10.x*c21.x*c22.x*c13y3 - 2*c10.x*c12y3*c13.x*c23.x + 6*c20.x*c21.x*c22.x*c13y3 +
            2*c20.x*c12y3*c13.x*c23.x + 2*c21.x*c12y3*c13.x*c22.x + 2*c10.y*c12x3*c13.y*c23.y - 6*c10.x*c10.y*c13.x*c13y2*c23.x +
            3*c10.x*c11.x*c12.x*c13y2*c23.y - 2*c10.x*c11.x*c12.y*c13y2*c23.x - 4*c10.x*c11.y*c12.x*c13y2*c23.x +
            3*c10.y*c11.x*c12.x*c13y2*c23.x + 6*c10.x*c10.y*c13x2*c13.y*c23.y + 6*c10.x*c20.x*c13.x*c13y2*c23.y -
            3*c10.x*c11.y*c12.y*c13x2*c23.y + 2*c10.x*c12.x*c12y2*c13.x*c23.y + 2*c10.x*c12.x*c12y2*c13.y*c23.x +
            6*c10.x*c20.y*c13.x*c13y2*c23.x + 6*c10.x*c21.x*c13.x*c13y2*c22.y + 6*c10.x*c13.x*c21.y*c22.x*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c23.y + 6*c10.y*c20.x*c13.x*c13y2*c23.x + 2*c10.y*c11.y*c12.x*c13x2*c23.y -
            3*c10.y*c11.y*c12.y*c13x2*c23.x + 2*c10.y*c12.x*c12y2*c13.x*c23.x + 6*c10.y*c21.x*c13.x*c22.x*c13y2 -
            3*c11.x*c20.x*c12.x*c13y2*c23.y + 2*c11.x*c20.x*c12.y*c13y2*c23.x + c11.x*c11.y*c12y2*c13.x*c23.x -
            3*c11.x*c12.x*c20.y*c13y2*c23.x - 3*c11.x*c12.x*c21.x*c13y2*c22.y - 3*c11.x*c12.x*c21.y*c22.x*c13y2 +
            2*c11.x*c21.x*c12.y*c22.x*c13y2 + 4*c20.x*c11.y*c12.x*c13y2*c23.x + 4*c11.y*c12.x*c21.x*c22.x*c13y2 -
            2*c10.x*c12x2*c12.y*c13.y*c23.y - 6*c10.y*c20.x*c13x2*c13.y*c23.y - 6*c10.y*c20.y*c13x2*c13.y*c23.x -
            6*c10.y*c21.x*c13x2*c13.y*c22.y - 2*c10.y*c12x2*c12.y*c13.x*c23.y - 2*c10.y*c12x2*c12.y*c13.y*c23.x -
            6*c10.y*c13x2*c21.y*c22.x*c13.y - c11.x*c11.y*c12x2*c13.y*c23.y - 2*c11.x*c11y2*c13.x*c13.y*c23.x +
            3*c20.x*c11.y*c12.y*c13x2*c23.y - 2*c20.x*c12.x*c12y2*c13.x*c23.y - 2*c20.x*c12.x*c12y2*c13.y*c23.x -
            6*c20.x*c20.y*c13.x*c13y2*c23.x - 6*c20.x*c21.x*c13.x*c13y2*c22.y - 6*c20.x*c13.x*c21.y*c22.x*c13y2 +
            3*c11.y*c20.y*c12.y*c13x2*c23.x + 3*c11.y*c21.x*c12.y*c13x2*c22.y + 3*c11.y*c12.y*c13x2*c21.y*c22.x -
            2*c12.x*c20.y*c12y2*c13.x*c23.x - 2*c12.x*c21.x*c12y2*c13.x*c22.y - 2*c12.x*c21.x*c12y2*c22.x*c13.y -
            2*c12.x*c12y2*c13.x*c21.y*c22.x - 6*c20.y*c21.x*c13.x*c22.x*c13y2 - c11y2*c12.x*c12.y*c13.x*c23.x +
            2*c20.x*c12x2*c12.y*c13.y*c23.y + 6*c20.y*c13x2*c21.y*c22.x*c13.y + 2*c11x2*c11.y*c13.x*c13.y*c23.y +
            c11x2*c12.x*c12.y*c13.y*c23.y + 2*c12x2*c20.y*c12.y*c13.y*c23.x + 2*c12x2*c21.x*c12.y*c13.y*c22.y +
            2*c12x2*c12.y*c21.y*c22.x*c13.y + c21x3*c13y3 + 3*c10x2*c13y3*c23.x - 3*c10y2*c13x3*c23.y +
            3*c20x2*c13y3*c23.x + c11y3*c13x2*c23.x - c11x3*c13y2*c23.y - c11.x*c11y2*c13x2*c23.y +
            c11x2*c11.y*c13y2*c23.x - 3*c10x2*c13.x*c13y2*c23.y + 3*c10y2*c13x2*c13.y*c23.x - c11x2*c12y2*c13.x*c23.y +
            c11y2*c12x2*c13.y*c23.x - 3*c21x2*c13.x*c21.y*c13y2 - 3*c20x2*c13.x*c13y2*c23.y + 3*c20y2*c13x2*c13.y*c23.x +
            c11.x*c12.x*c13.x*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) + c12x3*c13.y*(-2*c20.y*c23.y - 2*c21.y*c22.y) +
            c10.y*c13x3*(6*c20.y*c23.y + 6*c21.y*c22.y) + c11.y*c12.x*c13x2*(-2*c20.y*c23.y - 2*c21.y*c22.y) +
            c12x2*c12.y*c13.x*(2*c20.y*c23.y + 2*c21.y*c22.y) + c11.x*c12.y*c13x2*(-4*c20.y*c23.y - 4*c21.y*c22.y) +
            c10.x*c13x2*c13.y*(-6*c20.y*c23.y - 6*c21.y*c22.y) + c20.x*c13x2*c13.y*(6*c20.y*c23.y + 6*c21.y*c22.y) +
            c21.x*c13x2*c13.y*(6*c20.y*c22.y + 3*c21y2) + c13x3*(-2*c20.y*c21.y*c22.y - c20y2*c23.y -
            c21.y*(2*c20.y*c22.y + c21y2) - c20.y*(2*c20.y*c23.y + 2*c21.y*c22.y)),
        -c10.x*c11.x*c12.y*c13.x*c13.y*c22.y + c10.x*c11.y*c12.x*c13.x*c13.y*c22.y + 6*c10.x*c11.y*c12.y*c13.x*c22.x*c13.y -
            6*c10.y*c11.x*c12.x*c13.x*c13.y*c22.y - c10.y*c11.x*c12.y*c13.x*c22.x*c13.y + c10.y*c11.y*c12.x*c13.x*c22.x*c13.y +
            c11.x*c11.y*c12.x*c12.y*c13.x*c22.y - c11.x*c11.y*c12.x*c12.y*c22.x*c13.y + c11.x*c20.x*c12.y*c13.x*c13.y*c22.y +
            c11.x*c20.y*c12.y*c13.x*c22.x*c13.y + c11.x*c21.x*c12.y*c13.x*c21.y*c13.y - c20.x*c11.y*c12.x*c13.x*c13.y*c22.y -
            6*c20.x*c11.y*c12.y*c13.x*c22.x*c13.y - c11.y*c12.x*c20.y*c13.x*c22.x*c13.y - c11.y*c12.x*c21.x*c13.x*c21.y*c13.y -
            6*c10.x*c20.x*c22.x*c13y3 - 2*c10.x*c12y3*c13.x*c22.x + 2*c20.x*c12y3*c13.x*c22.x + 2*c10.y*c12x3*c13.y*c22.y -
            6*c10.x*c10.y*c13.x*c22.x*c13y2 + 3*c10.x*c11.x*c12.x*c13y2*c22.y - 2*c10.x*c11.x*c12.y*c22.x*c13y2 -
            4*c10.x*c11.y*c12.x*c22.x*c13y2 + 3*c10.y*c11.x*c12.x*c22.x*c13y2 + 6*c10.x*c10.y*c13x2*c13.y*c22.y +
            6*c10.x*c20.x*c13.x*c13y2*c22.y - 3*c10.x*c11.y*c12.y*c13x2*c22.y + 2*c10.x*c12.x*c12y2*c13.x*c22.y +
            2*c10.x*c12.x*c12y2*c22.x*c13.y + 6*c10.x*c20.y*c13.x*c22.x*c13y2 + 6*c10.x*c21.x*c13.x*c21.y*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c22.y + 6*c10.y*c20.x*c13.x*c22.x*c13y2 + 2*c10.y*c11.y*c12.x*c13x2*c22.y -
            3*c10.y*c11.y*c12.y*c13x2*c22.x + 2*c10.y*c12.x*c12y2*c13.x*c22.x - 3*c11.x*c20.x*c12.x*c13y2*c22.y +
            2*c11.x*c20.x*c12.y*c22.x*c13y2 + c11.x*c11.y*c12y2*c13.x*c22.x - 3*c11.x*c12.x*c20.y*c22.x*c13y2 -
            3*c11.x*c12.x*c21.x*c21.y*c13y2 + 4*c20.x*c11.y*c12.x*c22.x*c13y2 - 2*c10.x*c12x2*c12.y*c13.y*c22.y -
            6*c10.y*c20.x*c13x2*c13.y*c22.y - 6*c10.y*c20.y*c13x2*c22.x*c13.y - 6*c10.y*c21.x*c13x2*c21.y*c13.y -
            2*c10.y*c12x2*c12.y*c13.x*c22.y - 2*c10.y*c12x2*c12.y*c22.x*c13.y - c11.x*c11.y*c12x2*c13.y*c22.y -
            2*c11.x*c11y2*c13.x*c22.x*c13.y + 3*c20.x*c11.y*c12.y*c13x2*c22.y - 2*c20.x*c12.x*c12y2*c13.x*c22.y -
            2*c20.x*c12.x*c12y2*c22.x*c13.y - 6*c20.x*c20.y*c13.x*c22.x*c13y2 - 6*c20.x*c21.x*c13.x*c21.y*c13y2 +
            3*c11.y*c20.y*c12.y*c13x2*c22.x + 3*c11.y*c21.x*c12.y*c13x2*c21.y - 2*c12.x*c20.y*c12y2*c13.x*c22.x -
            2*c12.x*c21.x*c12y2*c13.x*c21.y - c11y2*c12.x*c12.y*c13.x*c22.x + 2*c20.x*c12x2*c12.y*c13.y*c22.y -
            3*c11.y*c21x2*c12.y*c13.x*c13.y + 6*c20.y*c21.x*c13x2*c21.y*c13.y + 2*c11x2*c11.y*c13.x*c13.y*c22.y +
            c11x2*c12.x*c12.y*c13.y*c22.y + 2*c12x2*c20.y*c12.y*c22.x*c13.y + 2*c12x2*c21.x*c12.y*c21.y*c13.y -
            3*c10.x*c21x2*c13y3 + 3*c20.x*c21x2*c13y3 + 3*c10x2*c22.x*c13y3 - 3*c10y2*c13x3*c22.y + 3*c20x2*c22.x*c13y3 +
            c21x2*c12y3*c13.x + c11y3*c13x2*c22.x - c11x3*c13y2*c22.y + 3*c10.y*c21x2*c13.x*c13y2 -
            c11.x*c11y2*c13x2*c22.y + c11.x*c21x2*c12.y*c13y2 + 2*c11.y*c12.x*c21x2*c13y2 + c11x2*c11.y*c22.x*c13y2 -
            c12.x*c21x2*c12y2*c13.y - 3*c20.y*c21x2*c13.x*c13y2 - 3*c10x2*c13.x*c13y2*c22.y + 3*c10y2*c13x2*c22.x*c13.y -
            c11x2*c12y2*c13.x*c22.y + c11y2*c12x2*c22.x*c13.y - 3*c20x2*c13.x*c13y2*c22.y + 3*c20y2*c13x2*c22.x*c13.y +
            c12x2*c12.y*c13.x*(2*c20.y*c22.y + c21y2) + c11.x*c12.x*c13.x*c13.y*(6*c20.y*c22.y + 3*c21y2) +
            c12x3*c13.y*(-2*c20.y*c22.y - c21y2) + c10.y*c13x3*(6*c20.y*c22.y + 3*c21y2) +
            c11.y*c12.x*c13x2*(-2*c20.y*c22.y - c21y2) + c11.x*c12.y*c13x2*(-4*c20.y*c22.y - 2*c21y2) +
            c10.x*c13x2*c13.y*(-6*c20.y*c22.y - 3*c21y2) + c20.x*c13x2*c13.y*(6*c20.y*c22.y + 3*c21y2) +
            c13x3*(-2*c20.y*c21y2 - c20y2*c22.y - c20.y*(2*c20.y*c22.y + c21y2)),
        -c10.x*c11.x*c12.y*c13.x*c21.y*c13.y + c10.x*c11.y*c12.x*c13.x*c21.y*c13.y + 6*c10.x*c11.y*c21.x*c12.y*c13.x*c13.y -
            6*c10.y*c11.x*c12.x*c13.x*c21.y*c13.y - c10.y*c11.x*c21.x*c12.y*c13.x*c13.y + c10.y*c11.y*c12.x*c21.x*c13.x*c13.y -
            c11.x*c11.y*c12.x*c21.x*c12.y*c13.y + c11.x*c11.y*c12.x*c12.y*c13.x*c21.y + c11.x*c20.x*c12.y*c13.x*c21.y*c13.y +
            6*c11.x*c12.x*c20.y*c13.x*c21.y*c13.y + c11.x*c20.y*c21.x*c12.y*c13.x*c13.y - c20.x*c11.y*c12.x*c13.x*c21.y*c13.y -
            6*c20.x*c11.y*c21.x*c12.y*c13.x*c13.y - c11.y*c12.x*c20.y*c21.x*c13.x*c13.y - 6*c10.x*c20.x*c21.x*c13y3 -
            2*c10.x*c21.x*c12y3*c13.x + 6*c10.y*c20.y*c13x3*c21.y + 2*c20.x*c21.x*c12y3*c13.x + 2*c10.y*c12x3*c21.y*c13.y -
            2*c12x3*c20.y*c21.y*c13.y - 6*c10.x*c10.y*c21.x*c13.x*c13y2 + 3*c10.x*c11.x*c12.x*c21.y*c13y2 -
            2*c10.x*c11.x*c21.x*c12.y*c13y2 - 4*c10.x*c11.y*c12.x*c21.x*c13y2 + 3*c10.y*c11.x*c12.x*c21.x*c13y2 +
            6*c10.x*c10.y*c13x2*c21.y*c13.y + 6*c10.x*c20.x*c13.x*c21.y*c13y2 - 3*c10.x*c11.y*c12.y*c13x2*c21.y +
            2*c10.x*c12.x*c21.x*c12y2*c13.y + 2*c10.x*c12.x*c12y2*c13.x*c21.y + 6*c10.x*c20.y*c21.x*c13.x*c13y2 +
            4*c10.y*c11.x*c12.y*c13x2*c21.y + 6*c10.y*c20.x*c21.x*c13.x*c13y2 + 2*c10.y*c11.y*c12.x*c13x2*c21.y -
            3*c10.y*c11.y*c21.x*c12.y*c13x2 + 2*c10.y*c12.x*c21.x*c12y2*c13.x - 3*c11.x*c20.x*c12.x*c21.y*c13y2 +
            2*c11.x*c20.x*c21.x*c12.y*c13y2 + c11.x*c11.y*c21.x*c12y2*c13.x - 3*c11.x*c12.x*c20.y*c21.x*c13y2 +
            4*c20.x*c11.y*c12.x*c21.x*c13y2 - 6*c10.x*c20.y*c13x2*c21.y*c13.y - 2*c10.x*c12x2*c12.y*c21.y*c13.y -
            6*c10.y*c20.x*c13x2*c21.y*c13.y - 6*c10.y*c20.y*c21.x*c13x2*c13.y - 2*c10.y*c12x2*c21.x*c12.y*c13.y -
            2*c10.y*c12x2*c12.y*c13.x*c21.y - c11.x*c11.y*c12x2*c21.y*c13.y - 4*c11.x*c20.y*c12.y*c13x2*c21.y -
            2*c11.x*c11y2*c21.x*c13.x*c13.y + 3*c20.x*c11.y*c12.y*c13x2*c21.y - 2*c20.x*c12.x*c21.x*c12y2*c13.y -
            2*c20.x*c12.x*c12y2*c13.x*c21.y - 6*c20.x*c20.y*c21.x*c13.x*c13y2 - 2*c11.y*c12.x*c20.y*c13x2*c21.y +
            3*c11.y*c20.y*c21.x*c12.y*c13x2 - 2*c12.x*c20.y*c21.x*c12y2*c13.x - c11y2*c12.x*c21.x*c12.y*c13.x +
            6*c20.x*c20.y*c13x2*c21.y*c13.y + 2*c20.x*c12x2*c12.y*c21.y*c13.y + 2*c11x2*c11.y*c13.x*c21.y*c13.y +
            c11x2*c12.x*c12.y*c21.y*c13.y + 2*c12x2*c20.y*c21.x*c12.y*c13.y + 2*c12x2*c20.y*c12.y*c13.x*c21.y +
            3*c10x2*c21.x*c13y3 - 3*c10y2*c13x3*c21.y + 3*c20x2*c21.x*c13y3 + c11y3*c21.x*c13x2 - c11x3*c21.y*c13y2 -
            3*c20y2*c13x3*c21.y - c11.x*c11y2*c13x2*c21.y + c11x2*c11.y*c21.x*c13y2 - 3*c10x2*c13.x*c21.y*c13y2 +
            3*c10y2*c21.x*c13x2*c13.y - c11x2*c12y2*c13.x*c21.y + c11y2*c12x2*c21.x*c13.y - 3*c20x2*c13.x*c21.y*c13y2 +
            3*c20y2*c21.x*c13x2*c13.y,
        c10.x*c10.y*c11.x*c12.y*c13.x*c13.y - c10.x*c10.y*c11.y*c12.x*c13.x*c13.y + c10.x*c11.x*c11.y*c12.x*c12.y*c13.y -
            c10.y*c11.x*c11.y*c12.x*c12.y*c13.x - c10.x*c11.x*c20.y*c12.y*c13.x*c13.y + 6*c10.x*c20.x*c11.y*c12.y*c13.x*c13.y +
            c10.x*c11.y*c12.x*c20.y*c13.x*c13.y - c10.y*c11.x*c20.x*c12.y*c13.x*c13.y - 6*c10.y*c11.x*c12.x*c20.y*c13.x*c13.y +
            c10.y*c20.x*c11.y*c12.x*c13.x*c13.y - c11.x*c20.x*c11.y*c12.x*c12.y*c13.y + c11.x*c11.y*c12.x*c20.y*c12.y*c13.x +
            c11.x*c20.x*c20.y*c12.y*c13.x*c13.y - c20.x*c11.y*c12.x*c20.y*c13.x*c13.y - 2*c10.x*c20.x*c12y3*c13.x +
            2*c10.y*c12x3*c20.y*c13.y - 3*c10.x*c10.y*c11.x*c12.x*c13y2 - 6*c10.x*c10.y*c20.x*c13.x*c13y2 +
            3*c10.x*c10.y*c11.y*c12.y*c13x2 - 2*c10.x*c10.y*c12.x*c12y2*c13.x - 2*c10.x*c11.x*c20.x*c12.y*c13y2 -
            c10.x*c11.x*c11.y*c12y2*c13.x + 3*c10.x*c11.x*c12.x*c20.y*c13y2 - 4*c10.x*c20.x*c11.y*c12.x*c13y2 +
            3*c10.y*c11.x*c20.x*c12.x*c13y2 + 6*c10.x*c10.y*c20.y*c13x2*c13.y + 2*c10.x*c10.y*c12x2*c12.y*c13.y +
            2*c10.x*c11.x*c11y2*c13.x*c13.y + 2*c10.x*c20.x*c12.x*c12y2*c13.y + 6*c10.x*c20.x*c20.y*c13.x*c13y2 -
            3*c10.x*c11.y*c20.y*c12.y*c13x2 + 2*c10.x*c12.x*c20.y*c12y2*c13.x + c10.x*c11y2*c12.x*c12.y*c13.x +
            c10.y*c11.x*c11.y*c12x2*c13.y + 4*c10.y*c11.x*c20.y*c12.y*c13x2 - 3*c10.y*c20.x*c11.y*c12.y*c13x2 +
            2*c10.y*c20.x*c12.x*c12y2*c13.x + 2*c10.y*c11.y*c12.x*c20.y*c13x2 + c11.x*c20.x*c11.y*c12y2*c13.x -
            3*c11.x*c20.x*c12.x*c20.y*c13y2 - 2*c10.x*c12x2*c20.y*c12.y*c13.y - 6*c10.y*c20.x*c20.y*c13x2*c13.y -
            2*c10.y*c20.x*c12x2*c12.y*c13.y - 2*c10.y*c11x2*c11.y*c13.x*c13.y - c10.y*c11x2*c12.x*c12.y*c13.y -
            2*c10.y*c12x2*c20.y*c12.y*c13.x - 2*c11.x*c20.x*c11y2*c13.x*c13.y - c11.x*c11.y*c12x2*c20.y*c13.y +
            3*c20.x*c11.y*c20.y*c12.y*c13x2 - 2*c20.x*c12.x*c20.y*c12y2*c13.x - c20.x*c11y2*c12.x*c12.y*c13.x +
            3*c10y2*c11.x*c12.x*c13.x*c13.y + 3*c11.x*c12.x*c20y2*c13.x*c13.y + 2*c20.x*c12x2*c20.y*c12.y*c13.y -
            3*c10x2*c11.y*c12.y*c13.x*c13.y + 2*c11x2*c11.y*c20.y*c13.x*c13.y + c11x2*c12.x*c20.y*c12.y*c13.y -
            3*c20x2*c11.y*c12.y*c13.x*c13.y - c10x3*c13y3 + c10y3*c13x3 + c20x3*c13y3 - c20y3*c13x3 -
            3*c10.x*c20x2*c13y3 - c10.x*c11y3*c13x2 + 3*c10x2*c20.x*c13y3 + c10.y*c11x3*c13y2 +
            3*c10.y*c20y2*c13x3 + c20.x*c11y3*c13x2 + c10x2*c12y3*c13.x - 3*c10y2*c20.y*c13x3 - c10y2*c12x3*c13.y +
            c20x2*c12y3*c13.x - c11x3*c20.y*c13y2 - c12x3*c20y2*c13.y - c10.x*c11x2*c11.y*c13y2 +
            c10.y*c11.x*c11y2*c13x2 - 3*c10.x*c10y2*c13x2*c13.y - c10.x*c11y2*c12x2*c13.y + c10.y*c11x2*c12y2*c13.x -
            c11.x*c11y2*c20.y*c13x2 + 3*c10x2*c10.y*c13.x*c13y2 + c10x2*c11.x*c12.y*c13y2 +
            2*c10x2*c11.y*c12.x*c13y2 - 2*c10y2*c11.x*c12.y*c13x2 - c10y2*c11.y*c12.x*c13x2 + c11x2*c20.x*c11.y*c13y2 -
            3*c10.x*c20y2*c13x2*c13.y + 3*c10.y*c20x2*c13.x*c13y2 + c11.x*c20x2*c12.y*c13y2 - 2*c11.x*c20y2*c12.y*c13x2 +
            c20.x*c11y2*c12x2*c13.y - c11.y*c12.x*c20y2*c13x2 - c10x2*c12.x*c12y2*c13.y - 3*c10x2*c20.y*c13.x*c13y2 +
            3*c10y2*c20.x*c13x2*c13.y + c10y2*c12x2*c12.y*c13.x - c11x2*c20.y*c12y2*c13.x + 2*c20x2*c11.y*c12.x*c13y2 +
            3*c20.x*c20y2*c13x2*c13.y - c20x2*c12.x*c12y2*c13.y - 3*c20x2*c20.y*c13.x*c13y2 + c12x2*c20y2*c12.y*c13.x
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var s = roots[i];
        var xRoots = new Polynomial(
            c13.x,
            c12.x,
            c11.x,
            c10.x - c20.x - s*c21.x - s*s*c22.x - s*s*s*c23.x
        ).getRoots();
        var yRoots = new Polynomial(
            c13.y,
            c12.y,
            c11.y,
            c10.y - c20.y - s*c21.y - s*s*c22.y - s*s*s*c23.y
        ).getRoots();

        if ( xRoots.length > 0 && yRoots.length > 0 ) {
            var TOLERANCE = 1e-4;

            checkRoots:
            for ( var j = 0; j < xRoots.length; j++ ) {
                var xRoot = xRoots[j];
                
                if ( 0 <= xRoot && xRoot <= 1 ) {
                    for ( var k = 0; k < yRoots.length; k++ ) {
                        if ( Math.abs( xRoot - yRoots[k] ) < TOLERANCE ) {
                            result.points.push(
                                c23.multiply(s*s*s).add(c22.multiply(s*s).add(c21.multiply(s).add(c20)))
                            );
                            break checkRoots;
                        }
                    }
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Circle
*
*****/
Intersection.intersectBezier3Circle = function(p1, p2, p3, p4, c, r) {
    return Intersection.intersectBezier3Ellipse(p1, p2, p3, p4, c, r, r);
};


/*****
*
*   intersectBezier3Ellipse
*
*****/
Intersection.intersectBezier3Ellipse = function(p1, p2, p3, p4, ec, rx, ry) {
    var a, b, c, d;       // temporary variables
    var c3, c2, c1, c0;   // coefficients of cubic
    var result = new Intersection("No Intersection");

    // Calculate the coefficients of cubic polynomial
    a = p1.multiply(-1);
    b = p2.multiply(3);
    c = p3.multiply(-3);
    d = a.add(b.add(c.add(p4)));
    c3 = new Vector2D(d.x, d.y);

    a = p1.multiply(3);
    b = p2.multiply(-6);
    c = p3.multiply(3);
    d = a.add(b.add(c));
    c2 = new Vector2D(d.x, d.y);

    a = p1.multiply(-3);
    b = p2.multiply(3);
    c = a.add(b);
    c1 = new Vector2D(c.x, c.y);

    c0 = new Vector2D(p1.x, p1.y);

    var rxrx  = rx*rx;
    var ryry  = ry*ry;
    var poly = new Polynomial(
        c3.x*c3.x*ryry + c3.y*c3.y*rxrx,
        2*(c3.x*c2.x*ryry + c3.y*c2.y*rxrx),
        2*(c3.x*c1.x*ryry + c3.y*c1.y*rxrx) + c2.x*c2.x*ryry + c2.y*c2.y*rxrx,
        2*c3.x*ryry*(c0.x - ec.x) + 2*c3.y*rxrx*(c0.y - ec.y) +
            2*(c2.x*c1.x*ryry + c2.y*c1.y*rxrx),
        2*c2.x*ryry*(c0.x - ec.x) + 2*c2.y*rxrx*(c0.y - ec.y) +
            c1.x*c1.x*ryry + c1.y*c1.y*rxrx,
        2*c1.x*ryry*(c0.x - ec.x) + 2*c1.y*rxrx*(c0.y - ec.y),
        c0.x*c0.x*ryry - 2*c0.y*ec.y*rxrx - 2*c0.x*ec.x*ryry +
            c0.y*c0.y*rxrx + ec.x*ec.x*ryry + ec.y*ec.y*rxrx - rxrx*ryry
    );
    var roots = poly.getRootsInInterval(0,1);

    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        result.points.push(
            c3.multiply(t*t*t).add(c2.multiply(t*t).add(c1.multiply(t).add(c0)))
        );
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Line
*
*   Many thanks to Dan Sunday at SoftSurfer.com.  He gave me a very thorough
*   sketch of the algorithm used here.  Without his help, I'm not sure when I
*   would have figured out this intersection problem.
*
*****/
Intersection.intersectBezier3Line = function(p1, p2, p3, p4, a1, a2) {
    var a, b, c, d;       // temporary variables
    var c3, c2, c1, c0;   // coefficients of cubic
    var cl;               // c coefficient for normal form of line
    var n;                // normal for normal form of line
    var min = a1.min(a2); // used to determine if point is on line segment
    var max = a1.max(a2); // used to determine if point is on line segment
    var result = new Intersection("No Intersection");
    
    // Start with Bezier using Bernstein polynomials for weighting functions:
    //     (1-t^3)P1 + 3t(1-t)^2P2 + 3t^2(1-t)P3 + t^3P4
    //
    // Expand and collect terms to form linear combinations of original Bezier
    // controls.  This ends up with a vector cubic in t:
    //     (-P1+3P2-3P3+P4)t^3 + (3P1-6P2+3P3)t^2 + (-3P1+3P2)t + P1
    //             /\                  /\                /\       /\
    //             ||                  ||                ||       ||
    //             c3                  c2                c1       c0
    
    // Calculate the coefficients
    a = p1.multiply(-1);
    b = p2.multiply(3);
    c = p3.multiply(-3);
    d = a.add(b.add(c.add(p4)));
    c3 = new Vector2D(d.x, d.y);

    a = p1.multiply(3);
    b = p2.multiply(-6);
    c = p3.multiply(3);
    d = a.add(b.add(c));
    c2 = new Vector2D(d.x, d.y);

    a = p1.multiply(-3);
    b = p2.multiply(3);
    c = a.add(b);
    c1 = new Vector2D(c.x, c.y);

    c0 = new Vector2D(p1.x, p1.y);
    
    // Convert line to normal form: ax + by + c = 0
    // Find normal to line: negative inverse of original line's slope
    n = new Vector2D(a1.y - a2.y, a2.x - a1.x);
    
    // Determine new c coefficient
    cl = a1.x*a2.y - a2.x*a1.y;

    // ?Rotate each cubic coefficient using line for new coordinate system?
    // Find roots of rotated cubic
    roots = new Polynomial(
        n.dot(c3),
        n.dot(c2),
        n.dot(c1),
        n.dot(c0) + cl
    ).getRoots();

    // Any roots in closed interval [0,1] are intersections on Bezier, but
    // might not be on the line segment.
    // Find intersections and calculate point coordinates
    for ( var i = 0; i < roots.length; i++ ) {
        var t = roots[i];

        if ( 0 <= t && t <= 1 ) {
            // We're within the Bezier curve
            // Find point on Bezier
            var p5 = p1.lerp(p2, t);
            var p6 = p2.lerp(p3, t);
            var p7 = p3.lerp(p4, t);

            var p8 = p5.lerp(p6, t);
            var p9 = p6.lerp(p7, t);

            var p10 = p8.lerp(p9, t);

            // See if point is on line segment
            // Had to make special cases for vertical and horizontal lines due
            // to slight errors in calculation of p10
            if ( a1.x == a2.x ) {
                if ( min.y <= p10.y && p10.y <= max.y ) {
                    result.status = "Intersection";
                    result.appendPoint( p10 );
                }
            } else if ( a1.y == a2.y ) {
                if ( min.x <= p10.x && p10.x <= max.x ) {
                    result.status = "Intersection";
                    result.appendPoint( p10 );
                }
            } else if ( p10.gte(min) && p10.lte(max) ) {
                result.status = "Intersection";
                result.appendPoint( p10 );
            }
        }
    }

    return result;
};


/*****
*
*   intersectBezier3Polygon
*
*****/
Intersection.intersectBezier3Polygon = function(p1, p2, p3, p4, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];
        var inter = Intersection.intersectBezier3Line(p1, p2, p3, p4, a1, a2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectBezier3Rectangle
*
*****/
Intersection.intersectBezier3Rectangle = function(p1, p2, p3, p4, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectBezier3Line(p1, p2, p3, p4, min, topRight);
    var inter2 = Intersection.intersectBezier3Line(p1, p2, p3, p4, topRight, max);
    var inter3 = Intersection.intersectBezier3Line(p1, p2, p3, p4, max, bottomLeft);
    var inter4 = Intersection.intersectBezier3Line(p1, p2, p3, p4, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectCircleCircle
*
*****/
Intersection.intersectCircleCircle = function(c1, r1, c2, r2) {
    var result;
    
    // Determine minimum and maximum radii where circles can intersect
    var r_max = r1 + r2;
    var r_min = Math.abs(r1 - r2);
    
    // Determine actual distance between circle circles
    var c_dist = c1.distanceFrom( c2 );

    if ( c_dist > r_max ) {
        result = new Intersection("Outside");
    } else if ( c_dist < r_min ) {
        result = new Intersection("Inside");
    } else {
        result = new Intersection("Intersection");

        var a = (r1*r1 - r2*r2 + c_dist*c_dist) / ( 2*c_dist );
        var h = Math.sqrt(r1*r1 - a*a);
        var p = c1.lerp(c2, a/c_dist);
        var b = h / c_dist;

        result.points.push(
            new Point2D(
                p.x - b * (c2.y - c1.y),
                p.y + b * (c2.x - c1.x)
            )
        );
        result.points.push(
            new Point2D(
                p.x + b * (c2.y - c1.y),
                p.y - b * (c2.x - c1.x)
            )
        );
    }

    return result;
};


/*****
*
*   intersectCircleEllipse
*
*****/
Intersection.intersectCircleEllipse = function(cc, r, ec, rx, ry) {
    return Intersection.intersectEllipseEllipse(cc, r, r, ec, rx, ry);
};


/*****
*
*   intersectCircleLine
*
*****/
Intersection.intersectCircleLine = function(c, r, a1, a2) {
    var result;
    var a  = (a2.x - a1.x) * (a2.x - a1.x) +
             (a2.y - a1.y) * (a2.y - a1.y);
    var b  = 2 * ( (a2.x - a1.x) * (a1.x - c.x) +
                   (a2.y - a1.y) * (a1.y - c.y)   );
    var cc = c.x*c.x + c.y*c.y + a1.x*a1.x + a1.y*a1.y -
             2 * (c.x * a1.x + c.y * a1.y) - r*r;
    var deter = b*b - 4*a*cc;

    if ( deter < 0 ) {
        result = new Intersection("Outside");
    } else if ( deter == 0 ) {
        result = new Intersection("Tangent");
        // NOTE: should calculate this point
    } else {
        var e  = Math.sqrt(deter);
        var u1 = ( -b + e ) / ( 2*a );
        var u2 = ( -b - e ) / ( 2*a );

        if ( (u1 < 0 || u1 > 1) && (u2 < 0 || u2 > 1) ) {
            if ( (u1 < 0 && u2 < 0) || (u1 > 1 && u2 > 1) ) {
                result = new Intersection("Outside");
            } else {
                result = new Intersection("Inside");
            }
        } else {
            result = new Intersection("Intersection");

            if ( 0 <= u1 && u1 <= 1)
                result.points.push( a1.lerp(a2, u1) );

            if ( 0 <= u2 && u2 <= 1)
                result.points.push( a1.lerp(a2, u2) );
        }
    }
    
    return result;
};


/*****
*
*   intersectCirclePolygon
*
*****/
Intersection.intersectCirclePolygon = function(c, r, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;
    var inter;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points[i];
        var a2 = points[(i+1) % length];

        inter = Intersection.intersectCircleLine(c, r, a1, a2);
        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";
    else
        result.status = inter.status;

    return result;
};


/*****
*
*   intersectCircleRectangle
*
*****/
Intersection.intersectCircleRectangle = function(c, r, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectCircleLine(c, r, min, topRight);
    var inter2 = Intersection.intersectCircleLine(c, r, topRight, max);
    var inter3 = Intersection.intersectCircleLine(c, r, max, bottomLeft);
    var inter4 = Intersection.intersectCircleLine(c, r, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";
    else
        result.status = inter1.status;

    return result;
};


/*****
*
*   intersectEllipseEllipse
*   
*   This code is based on MgcIntr2DElpElp.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*   NOTE: Rotation will need to be added to this function
*
*****/
Intersection.intersectEllipseEllipse = function(c1, rx1, ry1, c2, rx2, ry2) {
    var a = [
        ry1*ry1, 0, rx1*rx1, -2*ry1*ry1*c1.x, -2*rx1*rx1*c1.y,
        ry1*ry1*c1.x*c1.x + rx1*rx1*c1.y*c1.y - rx1*rx1*ry1*ry1
    ];
    var b = [
        ry2*ry2, 0, rx2*rx2, -2*ry2*ry2*c2.x, -2*rx2*rx2*c2.y,
        ry2*ry2*c2.x*c2.x + rx2*rx2*c2.y*c2.y - rx2*rx2*ry2*ry2
    ];

    var yPoly   = Intersection.bezout(a, b);
    var yRoots  = yPoly.getRoots();
    var epsilon = 1e-3;
    var norm0   = ( a[0]*a[0] + 2*a[1]*a[1] + a[2]*a[2] ) * epsilon;
    var norm1   = ( b[0]*b[0] + 2*b[1]*b[1] + b[2]*b[2] ) * epsilon;
    var result  = new Intersection("No Intersection");

    for ( var y = 0; y < yRoots.length; y++ ) {
        var xPoly = new Polynomial(
            a[0],
            a[3] + yRoots[y] * a[1],
            a[5] + yRoots[y] * (a[4] + yRoots[y]*a[2])
        );
        var xRoots = xPoly.getRoots();

        for ( var x = 0; x < xRoots.length; x++ ) {
            var test =
                ( a[0]*xRoots[x] + a[1]*yRoots[y] + a[3] ) * xRoots[x] + 
                ( a[2]*yRoots[y] + a[4] ) * yRoots[y] + a[5];
            if ( Math.abs(test) < norm0 ) {
                test =
                    ( b[0]*xRoots[x] + b[1]*yRoots[y] + b[3] ) * xRoots[x] +
                    ( b[2]*yRoots[y] + b[4] ) * yRoots[y] + b[5];
                if ( Math.abs(test) < norm1 ) {
                    result.appendPoint( new Point2D( xRoots[x], yRoots[y] ) );
                }
            }
        }
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectEllipseLine
*   
*   NOTE: Rotation will need to be added to this function
*
*****/
Intersection.intersectEllipseLine = function(c, rx, ry, a1, a2) {
    var result;
    var origin = new Vector2D(a1.x, a1.y);
    var dir    = Vector2D.fromPoints(a1, a2);
    var center = new Vector2D(c.x, c.y);
    var diff   = origin.subtract(center);
    var mDir   = new Vector2D( dir.x/(rx*rx),  dir.y/(ry*ry)  );
    var mDiff  = new Vector2D( diff.x/(rx*rx), diff.y/(ry*ry) );

    var a = dir.dot(mDir);
    var b = dir.dot(mDiff);
    var c = diff.dot(mDiff) - 1.0;
    var d = b*b - a*c;

    if ( d < 0 ) {
        result = new Intersection("Outside");
    } else if ( d > 0 ) {
        var root = Math.sqrt(d);
        var t_a  = (-b - root) / a;
        var t_b  = (-b + root) / a;

        if ( (t_a < 0 || 1 < t_a) && (t_b < 0 || 1 < t_b) ) {
            if ( (t_a < 0 && t_b < 0) || (t_a > 1 && t_b > 1) )
                result = new Intersection("Outside");
            else
                result = new Intersection("Inside");
        } else {
            result = new Intersection("Intersection");
            if ( 0 <= t_a && t_a <= 1 )
                result.appendPoint( a1.lerp(a2, t_a) );
            if ( 0 <= t_b && t_b <= 1 )
                result.appendPoint( a1.lerp(a2, t_b) );
        }
    } else {
        var t = -b/a;
        if ( 0 <= t && t <= 1 ) {
            result = new Intersection("Intersection");
            result.appendPoint( a1.lerp(a2, t) );
        } else {
            result = new Intersection("Outside");
        }
    }
    
    return result;
};


/*****
*
*   intersectEllipsePolygon
*
*****/
Intersection.intersectEllipsePolygon = function(c, rx, ry, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var b1 = points[i];
        var b2 = points[(i+1) % length];
        var inter = Intersection.intersectEllipseLine(c, rx, ry, b1, b2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectEllipseRectangle
*
*****/
Intersection.intersectEllipseRectangle = function(c, rx, ry, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectEllipseLine(c, rx, ry, min, topRight);
    var inter2 = Intersection.intersectEllipseLine(c, rx, ry, topRight, max);
    var inter3 = Intersection.intersectEllipseLine(c, rx, ry, max, bottomLeft);
    var inter4 = Intersection.intersectEllipseLine(c, rx, ry, bottomLeft, min);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectLineLine
*
*****/
Intersection.intersectLineLine = function(a1, a2, b1, b2) {
    var result;
    
    var ua_t = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
    var ub_t = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);
    var u_b  = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);

    if ( u_b != 0 ) {
        var ua = ua_t / u_b;
        var ub = ub_t / u_b;

        if ( 0 <= ua && ua <= 1 && 0 <= ub && ub <= 1 ) {
            result = new Intersection("Intersection");
            result.points.push(
                new Point2D(
                    a1.x + ua * (a2.x - a1.x),
                    a1.y + ua * (a2.y - a1.y)
                )
            );
        } else {
            result = new Intersection("No Intersection");
        }
    } else {
        if ( ua_t == 0 || ub_t == 0 ) {
            result = new Intersection("Coincident");
        } else {
            result = new Intersection("Parallel");
        }
    }

    return result;
};


/*****
*
*   intersectLinePolygon
*
*****/
Intersection.intersectLinePolygon = function(a1, a2, points) {
    var result = new Intersection("No Intersection");
    var length = points.length;

    for ( var i = 0; i < length; i++ ) {
        var b1 = points[i];
        var b2 = points[(i+1) % length];
        var inter = Intersection.intersectLineLine(a1, a2, b1, b2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   intersectLineRectangle
*
*****/
Intersection.intersectLineRectangle = function(a1, a2, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLineLine(min, topRight, a1, a2);
    var inter2 = Intersection.intersectLineLine(topRight, max, a1, a2);
    var inter3 = Intersection.intersectLineLine(max, bottomLeft, a1, a2);
    var inter4 = Intersection.intersectLineLine(bottomLeft, min, a1, a2);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectPolygonPolygon
*
*****/
Intersection.intersectPolygonPolygon = function(points1, points2) {
    var result = new Intersection("No Intersection");
    var length = points1.length;

    for ( var i = 0; i < length; i++ ) {
        var a1 = points1[i];
        var a2 = points1[(i+1) % length];
        var inter = Intersection.intersectLinePolygon(a1, a2, points2);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;

};


/*****
*
*   intersectPolygonRectangle
*
*****/
Intersection.intersectPolygonRectangle = function(points, r1, r2) {
    var min        = r1.min(r2);
    var max        = r1.max(r2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLinePolygon(min, topRight, points);
    var inter2 = Intersection.intersectLinePolygon(topRight, max, points);
    var inter3 = Intersection.intersectLinePolygon(max, bottomLeft, points);
    var inter4 = Intersection.intersectLinePolygon(bottomLeft, min, points);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   intersectRayRay
*
*****/
Intersection.intersectRayRay = function(a1, a2, b1, b2) {
    var result;
    
    var ua_t = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
    var ub_t = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);
    var u_b  = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);

    if ( u_b != 0 ) {
        var ua = ua_t / u_b;

        result = new Intersection("Intersection");
        result.points.push(
            new Point2D(
                a1.x + ua * (a2.x - a1.x),
                a1.y + ua * (a2.y - a1.y)
            )
        );
    } else {
        if ( ua_t == 0 || ub_t == 0 ) {
            result = new Intersection("Coincident");
        } else {
            result = new Intersection("Parallel");
        }
    }

    return result;
};


/*****
*
*   intersectRectangleRectangle
*
*****/
Intersection.intersectRectangleRectangle = function(a1, a2, b1, b2) {
    var min        = a1.min(a2);
    var max        = a1.max(a2);
    var topRight   = new Point2D( max.x, min.y );
    var bottomLeft = new Point2D( min.x, max.y );
    
    var inter1 = Intersection.intersectLineRectangle(min, topRight, b1, b2);
    var inter2 = Intersection.intersectLineRectangle(topRight, max, b1, b2);
    var inter3 = Intersection.intersectLineRectangle(max, bottomLeft, b1, b2);
    var inter4 = Intersection.intersectLineRectangle(bottomLeft, min, b1, b2);
    
    var result = new Intersection("No Intersection");

    result.appendPoints(inter1.points);
    result.appendPoints(inter2.points);
    result.appendPoints(inter3.points);
    result.appendPoints(inter4.points);

    if ( result.points.length > 0 )
        result.status = "Intersection";

    return result;
};


/*****
*
*   bezout
*
*   This code is based on MgcIntr2DElpElp.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*****/
Intersection.bezout = function(e1, e2) {
    var AB    = e1[0]*e2[1] - e2[0]*e1[1];
    var AC    = e1[0]*e2[2] - e2[0]*e1[2];
    var AD    = e1[0]*e2[3] - e2[0]*e1[3];
    var AE    = e1[0]*e2[4] - e2[0]*e1[4];
    var AF    = e1[0]*e2[5] - e2[0]*e1[5];
    var BC    = e1[1]*e2[2] - e2[1]*e1[2];
    var BE    = e1[1]*e2[4] - e2[1]*e1[4];
    var BF    = e1[1]*e2[5] - e2[1]*e1[5];
    var CD    = e1[2]*e2[3] - e2[2]*e1[3];
    var DE    = e1[3]*e2[4] - e2[3]*e1[4];
    var DF    = e1[3]*e2[5] - e2[3]*e1[5];
    var BFpDE = BF + DE;
    var BEmCD = BE - CD;

    return new Polynomial(
        AB*BC - AC*AC,
        AB*BEmCD + AD*BC - 2*AC*AE,
        AB*BFpDE + AD*BEmCD - AE*AE - 2*AC*AF,
        AB*DF + AD*BFpDE - 2*AE*AF,
        AD*DF - AF*AF
    );
};

/*****
*
*   IntersectionParams.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function IntersectionParams(name, params) {
    if ( arguments.length > 0 )
        this.init(name, params);
}


/*****
*
*   init
*
*****/
IntersectionParams.prototype.init = function(name, params) {
    this.name   = name;
    this.params = params;
};

/*****
*
*   Point2D.js
*
*   copyright 2001-2002, Kevin Lindsey
*
*****/

/*****
*
*   Point2D
*
*****/

/*****
*
*   constructor
*
*****/
function Point2D(x, y) {
    if ( arguments.length > 0 ) {
        this.init(x, y);
    }
}


/*****
*
*   init
*
*****/
Point2D.prototype.init = function(x, y) {
    this.x = x;
    this.y = y;
};


/*****
*
*   add
*
*****/
Point2D.prototype.add = function(that) {
    return new Point2D(this.x+that.x, this.y+that.y);
};


/*****
*
*   addEquals
*
*****/
Point2D.prototype.addEquals = function(that) {
    this.x += that.x;
    this.y += that.y;

    return this;
};


/*****
*
*   scalarAdd
*
*****/
Point2D.prototype.scalarAdd = function(scalar) {
    return new Point2D(this.x+scalar, this.y+scalar);
};


/*****
*
*   scalarAddEquals
*
*****/
Point2D.prototype.scalarAddEquals = function(scalar) {
    this.x += scalar;
    this.y += scalar;

    return this;
};


/*****
*
*   subtract
*
*****/
Point2D.prototype.subtract = function(that) {
    return new Point2D(this.x-that.x, this.y-that.y);
};


/*****
*
*   subtractEquals
*
*****/
Point2D.prototype.subtractEquals = function(that) {
    this.x -= that.x;
    this.y -= that.y;

    return this;
};


/*****
*
*   scalarSubtract
*
*****/
Point2D.prototype.scalarSubtract = function(scalar) {
    return new Point2D(this.x-scalar, this.y-scalar);
};


/*****
*
*   scalarSubtractEquals
*
*****/
Point2D.prototype.scalarSubtractEquals = function(scalar) {
    this.x -= scalar;
    this.y -= scalar;

    return this;
};


/*****
*
*   multiply
*
*****/
Point2D.prototype.multiply = function(scalar) {
    return new Point2D(this.x*scalar, this.y*scalar);
};


/*****
*
*   multiplyEquals
*
*****/
Point2D.prototype.multiplyEquals = function(scalar) {
    this.x *= scalar;
    this.y *= scalar;

    return this;
};


/*****
*
*   divide
*
*****/
Point2D.prototype.divide = function(scalar) {
    return new Point2D(this.x/scalar, this.y/scalar);
};


/*****
*
*   divideEquals
*
*****/
Point2D.prototype.divideEquals = function(scalar) {
    this.x /= scalar;
    this.y /= scalar;

    return this;
};


/*****
*
*   comparison methods
*
*****/

/*****
*
*   eq - equal
*
*****/
Point2D.prototype.eq = function(that) {
    return ( this.x == that.x && this.y == that.y );
};


/*****
*
*   lt - less than
*
*****/
Point2D.prototype.lt = function(that) {
    return ( this.x < that.x && this.y < that.y );
};


/*****
*
*   lte - less than or equal
*
*****/
Point2D.prototype.lte = function(that) {
    return ( this.x <= that.x && this.y <= that.y );
};


/*****
*
*   gt - greater than
*
*****/
Point2D.prototype.gt = function(that) {
    return ( this.x > that.x && this.y > that.y );
};


/*****
*
*   gte - greater than or equal
*
*****/
Point2D.prototype.gte = function(that) {
    return ( this.x >= that.x && this.y >= that.y );
};


/*****
*
*   utility methods
*
*****/

/*****
*
*   lerp
*
*****/
Point2D.prototype.lerp = function(that, t) {
    return new Point2D(
        this.x + (that.x - this.x) * t,
        this.y + (that.y - this.y) * t
    );
};


/*****
*
*   distanceFrom
*
*****/
Point2D.prototype.distanceFrom = function(that) {
    var dx = this.x - that.x;
    var dy = this.y - that.y;

    return Math.sqrt(dx*dx + dy*dy);
};


/*****
*
*   min
*
*****/
Point2D.prototype.min = function(that) {
    return new Point2D(
        Math.min( this.x, that.x ),
        Math.min( this.y, that.y )
    );
};


/*****
*
*   max
*
*****/
Point2D.prototype.max = function(that) {
    return new Point2D(
        Math.max( this.x, that.x ),
        Math.max( this.y, that.y )
    );
};


/*****
*
*   toString
*
*****/
Point2D.prototype.toString = function() {
    return this.x + "," + this.y;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   setXY
*
*****/
Point2D.prototype.setXY = function(x, y) {
    this.x = x;
    this.y = y;
};


/*****
*
*   setFromPoint
*
*****/
Point2D.prototype.setFromPoint = function(that) {
    this.x = that.x;
    this.y = that.y;
};


/*****
*
*   swap
*
*****/
Point2D.prototype.swap = function(that) {
    var x = this.x;
    var y = this.y;

    this.x = that.x;
    this.y = that.y;

    that.x = x;
    that.y = y;
};

/*****
*
*   Polynomial.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

Polynomial.TOLERANCE = 1e-6;
Polynomial.ACCURACY  = 6;


/*****
*
*   constructor
*
*****/
function Polynomial() {
    this.init( arguments );
}


/*****
*
*   init
*
*****/
Polynomial.prototype.init = function(coefs) {
    this.coefs = new Array();

    for ( var i = coefs.length - 1; i >= 0; i-- )
        this.coefs.push( coefs[i] );
};


/*****
*
*   eval
*
*****/
Polynomial.prototype.eval = function(x) {
    var result = 0;

    for ( var i = this.coefs.length - 1; i >= 0; i-- )
        result = result * x + this.coefs[i];

    return result;
};


/*****
*
*   multiply
*
*****/
Polynomial.prototype.multiply = function(that) {
    var result = new Polynomial();

    for ( var i = 0; i <= this.getDegree() + that.getDegree(); i++ )
        result.coefs.push(0);

    for ( var i = 0; i <= this.getDegree(); i++ )
        for ( var j = 0; j <= that.getDegree(); j++ )
            result.coefs[i+j] += this.coefs[i] * that.coefs[j];

    return result;
};


/*****
*
*   divide_scalar
*
*****/
Polynomial.prototype.divide_scalar = function(scalar) {
    for ( var i = 0; i < this.coefs.length; i++ )
        this.coefs[i] /= scalar;
};


/*****
*
*   simplify
*
*****/
Polynomial.prototype.simplify = function() {
    for ( var i = this.getDegree(); i >= 0; i-- ) {
        if ( Math.abs( this.coefs[i] ) <= Polynomial.TOLERANCE )
            this.coefs.pop();
        else
            break;
    }
};


/*****
*
*   bisection
*
*****/
Polynomial.prototype.bisection = function(min, max) {
    var minValue = this.eval(min);
    var maxValue = this.eval(max);
    var result;
    
    if ( Math.abs(minValue) <= Polynomial.TOLERANCE )
        result = min;
    else if ( Math.abs(maxValue) <= Polynomial.TOLERANCE )
        result = max;
    else if ( minValue * maxValue <= 0 ) {
        var tmp1  = Math.log(max - min);
        var tmp2  = Math.log(10) * Polynomial.ACCURACY;
        var iters = Math.ceil( (tmp1+tmp2) / Math.log(2) );

        for ( var i = 0; i < iters; i++ ) {
            result = 0.5 * (min + max);
            var value = this.eval(result);

            if ( Math.abs(value) <= Polynomial.TOLERANCE ) {
                break;
            }

            if ( value * minValue < 0 ) {
                max = result;
                maxValue = value;
            } else {
                min = result;
                minValue = value;
            }
        }
    }

    return result;
};


/*****
*
*   toString
*
*****/
Polynomial.prototype.toString = function() {
    var coefs = new Array();
    var signs = new Array();
    
    for ( var i = this.coefs.length - 1; i >= 0; i-- ) {
        var value = this.coefs[i];

        if ( value != 0 ) {
            var sign = ( value < 0 ) ? " - " : " + ";

            value = Math.abs(value);
            if ( i > 0 )
                if ( value == 1 )
                    value = "x";
                else
                    value += "x";
            if ( i > 1 ) value += "^" + i;

            signs.push( sign );
            coefs.push( value );
        }
    }

    signs[0] = ( signs[0] == " + " ) ? "" : "-";

    var result = "";
    for ( var i = 0; i < coefs.length; i++ )
        result += signs[i] + coefs[i];
    
    return result;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   get degree
*
*****/
Polynomial.prototype.getDegree = function() {
    return this.coefs.length - 1;
};


/*****
*
*   getDerivative
*
*****/
Polynomial.prototype.getDerivative = function() {
    var derivative = new Polynomial();

    for ( var i = 1; i < this.coefs.length; i++ ) {
        derivative.coefs.push(i*this.coefs[i]);
    }

    return derivative;
};


/*****
*
*   getRoots
*
*****/
Polynomial.prototype.getRoots = function() {
    var result;

    this.simplify();
    switch ( this.getDegree() ) {
        case 0: result = new Array();              break;
        case 1: result = this.getLinearRoot();     break;
        case 2: result = this.getQuadraticRoots(); break;
        case 3: result = this.getCubicRoots();     break;
        case 4: result = this.getQuarticRoots();   break;
        default:
            result = new Array();
            // should try Newton's method and/or bisection
    }

    return result;
};


/*****
*
*   getRootsInInterval
*
*****/
Polynomial.prototype.getRootsInInterval = function(min, max) {
    var roots = new Array();
    var root;

    if ( this.getDegree() == 1 ) {
        root = this.bisection(min, max);
        if ( root != null ) roots.push(root);
    } else {
        // get roots of derivative
        var deriv  = this.getDerivative();
        var droots = deriv.getRootsInInterval(min, max);

        if ( droots.length > 0 ) {
            // find root on [min, droots[0]]
            root = this.bisection(min, droots[0]);
            if ( root != null ) roots.push(root);

            // find root on [droots[i],droots[i+1]] for 0 <= i <= count-2
            for ( i = 0; i <= droots.length-2; i++ ) {
                root = this.bisection(droots[i], droots[i+1]);
                if ( root != null ) roots.push(root);
            }

            // find root on [droots[count-1],xmax]
            root = this.bisection(droots[droots.length-1], max);
            if ( root != null ) roots.push(root);
        } else {
            // polynomial is monotone on [min,max], has at most one root
            root = this.bisection(min, max);
            if ( root != null ) roots.push(root);
        }
    }

    return roots;
};


/*****
*
*   getLinearRoot
*
*****/
Polynomial.prototype.getLinearRoot = function() {
    var result = new Array();
    var a = this.coefs[1];
    
    if ( a != 0 )
        result.push( -this.coefs[0] / a );

    return result;
};


/*****
*
*   getQuadraticRoots
*
*****/
Polynomial.prototype.getQuadraticRoots = function() {
    var results = new Array();

    if ( this.getDegree() == 2 ) {
        var a = this.coefs[2];
        var b = this.coefs[1] / a;
        var c = this.coefs[0] / a;
        var d = b*b - 4*c;

        if ( d > 0 ) {
            var e = Math.sqrt(d);
            
            results.push( 0.5 * (-b + e) );
            results.push( 0.5 * (-b - e) );
        } else if ( d == 0 ) {
            // really two roots with same value, but we only return one
            results.push( 0.5 * -b );
        }
    }

    return results;
};


/*****
*
*   getCubicRoots
*
*   This code is based on MgcPolynomial.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*****/
Polynomial.prototype.getCubicRoots = function() {
    var results = new Array();

    if ( this.getDegree() == 3 ) {
        var c3 = this.coefs[3];
        var c2 = this.coefs[2] / c3;
        var c1 = this.coefs[1] / c3;
        var c0 = this.coefs[0] / c3;

        var a       = (3*c1 - c2*c2) / 3;
        var b       = (2*c2*c2*c2 - 9*c1*c2 + 27*c0) / 27;
        var offset  = c2 / 3;
        var discrim = b*b/4 + a*a*a/27;
        var halfB   = b / 2;

        if ( Math.abs(discrim) <= Polynomial.TOLERANCE ) disrim = 0;
        
        if ( discrim > 0 ) {
            var e = Math.sqrt(discrim);
            var tmp;
            var root;

            tmp = -halfB + e;
            if ( tmp >= 0 )
                root = Math.pow(tmp, 1/3);
            else
                root = -Math.pow(-tmp, 1/3);

            tmp = -halfB - e;
            if ( tmp >= 0 )
                root += Math.pow(tmp, 1/3);
            else
                root -= Math.pow(-tmp, 1/3);

            results.push( root - offset );
        } else if ( discrim < 0 ) {
            var distance = Math.sqrt(-a/3);
            var angle    = Math.atan2( Math.sqrt(-discrim), -halfB) / 3;
            var cos      = Math.cos(angle);
            var sin      = Math.sin(angle);
            var sqrt3    = Math.sqrt(3);

            results.push( 2*distance*cos - offset );
            results.push( -distance * (cos + sqrt3 * sin) - offset);
            results.push( -distance * (cos - sqrt3 * sin) - offset);
        } else {
            var tmp;

            if ( halfB >= 0 )
                tmp = -Math.pow(halfB, 1/3);
            else
                tmp = Math.pow(-halfB, 1/3);

            results.push( 2*tmp - offset );
            // really should return next root twice, but we return only one
            results.push( -tmp - offset );
        }
    }

    return results;
};


/*****
*
*   getQuarticRoots
*
*   This code is based on MgcPolynomial.cpp written by David Eberly.  His
*   code along with many other excellent examples are avaiable at his site:
*   http://www.magic-software.com
*
*****/
Polynomial.prototype.getQuarticRoots = function() {
    var results = new Array();

    if ( this.getDegree() == 4 ) {
        var c4 = this.coefs[4];
        var c3 = this.coefs[3] / c4;
        var c2 = this.coefs[2] / c4;
        var c1 = this.coefs[1] / c4;
        var c0 = this.coefs[0] / c4;

        var resolveRoots = new Polynomial(
            1, -c2, c3*c1 - 4*c0, -c3*c3*c0 + 4*c2*c0 -c1*c1
        ).getCubicRoots();
        var y       = resolveRoots[0];
        var discrim = c3*c3/4 - c2 + y;

        if ( Math.abs(discrim) <= Polynomial.TOLERANCE ) discrim = 0;

        if ( discrim > 0 ) {
            var e     = Math.sqrt(discrim);
            var t1    = 3*c3*c3/4 - e*e - 2*c2;
            var t2    = ( 4*c3*c2 - 8*c1 - c3*c3*c3 ) / ( 4*e );
            var plus  = t1+t2;
            var minus = t1-t2;

            if ( Math.abs(plus)  <= Polynomial.TOLERANCE ) plus  = 0;
            if ( Math.abs(minus) <= Polynomial.TOLERANCE ) minus = 0;

            if ( plus >= 0 ) {
                var f = Math.sqrt(plus);

                results.push( -c3/4 + (e+f)/2 );
                results.push( -c3/4 + (e-f)/2 );
            }
            if ( minus >= 0 ) {
                var f = Math.sqrt(minus);

                results.push( -c3/4 + (f-e)/2 );
                results.push( -c3/4 - (f+e)/2 );
            }
        } else if ( discrim < 0 ) {
            // no roots
        } else {
            var t2 = y*y - 4*c0;

            if ( t2 >= -Polynomial.TOLERANCE ) {
                if ( t2 < 0 ) t2 = 0;

                t2 = 2*Math.sqrt(t2);
                t1 = 3*c3*c3/4 - 2*c2;
                if ( t1+t2 >= Polynomial.TOLERANCE ) {
                    var d = Math.sqrt(t1+t2);

                    results.push( -c3/4 + d/2 );
                    results.push( -c3/4 - d/2 );
                }
                if ( t1-t2 >= Polynomial.TOLERANCE ) {
                    var d = Math.sqrt(t1-t2);

                    results.push( -c3/4 + d/2 );
                    results.push( -c3/4 - d/2 );
                }
            }
        }
    }

    return results;
};
/*****
*
*   Vector2D.js
*
*   copyright 2001-2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function Vector2D(x, y) {
    if ( arguments.length > 0 ) {
        this.init(x, y);
    }
}


/*****
*
*   init
*
*****/
Vector2D.prototype.init = function(x, y) {
    this.x = x;
    this.y = y;
};


/*****
*
*   length
*
*****/
Vector2D.prototype.length = function() {
    return Math.sqrt(this.x*this.x + this.y*this.y);
};


/*****
*
*   dot
*
*****/
Vector2D.prototype.dot = function(that) {
    return this.x*that.x + this.y*that.y;
};


/*****
*
*   cross
*
*****/
Vector2D.prototype.cross = function(that) {
    return this.x*that.y - this.y*that.x;
}


/*****
*
*   unit
*
*****/
Vector2D.prototype.unit = function() {
    return this.divide( this.length() );
};


/*****
*
*   unitEquals
*
*****/
Vector2D.prototype.unitEquals = function() {
    this.divideEquals( this.length() );

    return this;
};


/*****
*
*   add
*
*****/
Vector2D.prototype.add = function(that) {
    return new Vector2D(this.x + that.x, this.y + that.y);
};


/*****
*
*   addEquals
*
*****/
Vector2D.prototype.addEquals = function(that) {
    this.x += that.x;
    this.y += that.y;

    return this;
};


/*****
*
*   subtract
*
*****/
Vector2D.prototype.subtract = function(that) {
    return new Vector2D(this.x - that.x, this.y - that.y);
};


/*****
*
*   subtractEquals
*
*****/
Vector2D.prototype.subtractEquals = function(that) {
    this.x -= that.x;
    this.y -= that.y;

    return this;
};


/*****
*
*   multiply
*
*****/
Vector2D.prototype.multiply = function(scalar) {
    return new Vector2D(this.x * scalar, this.y * scalar);
};


/*****
*
*   multiplyEquals
*
*****/
Vector2D.prototype.multiplyEquals = function(scalar) {
    this.x *= scalar;
    this.y *= scalar;

    return this;
};


/*****
*
*   divide
*
*****/
Vector2D.prototype.divide = function(scalar) {
    return new Vector2D(this.x / scalar, this.y / scalar);
};


/*****
*
*   divideEquals
*
*****/
Vector2D.prototype.divideEquals = function(scalar) {
    this.x /= scalar;
    this.y /= scalar;

    return this;
};


/*****
*
*   fromPoints
*
*****/
Vector2D.fromPoints = function(p1, p2) {
    return new Vector2D(
        p2.x - p1.x,
        p2.y - p1.y
    );
};
/*****
*
*   Shape.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Shape.prototype             = new EventHandler();
Shape.prototype.constructor = Shape;
Shape.superclass            = EventHandler.prototype;


/*****
*
*   constructor
*
*****/
function Shape(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
Shape.prototype.init = function(svgNode) {
    this.svgNode = svgNode;

    this.locked   = false;
    this.visible  = true;
    this.selected = false;

    this.callback = null;

    this.lastUpdate = null;
}


/*****
*
*   show
*
*****/
Shape.prototype.show = function(state) {
    var display = ( state ) ? "inline" : "none";

    this.visible = state;
    this.svgNode.setAttributeNS(null, "display", display);
};


/*****
*
*   refresh
*
*****/
Shape.prototype.refresh = function() {
    // abstract method
};


/*****
*
*   update
*
*****/
Shape.prototype.update = function() {
    this.refresh();

    if ( this.owner ) this.owner.update(this);

    if ( this.callback != null ) this.callback(this);
};


/*****
*
*   translate
*
*****/
Shape.prototype.translate = function(delta) {
    // abstract method
};


/*****
*
*   select
*
*****/
Shape.prototype.select = function(state) {
    this.selected = state;
};


/*****
*
*   registerHandles
*
*****/
Shape.prototype.registerHandles = function() {
    // abstract method
};


/*****
*
*   unregisterHandles
*
*****/
Shape.prototype.unregisterHandles = function() {
    // abstract method
};


/*****
*
*   selectHandles
*
*****/
Shape.prototype.selectHandles = function(select) {
    // abstract method
};


/*****
*
*   showHandles
*
*****/
Shape.prototype.showHandles = function(state) {
    // abstract method
};


/*****
*
*   event handlers
*
*****/

/******
*
*   mousedown
*
*****/
Shape.prototype.mousedown = function(e) {
    if ( !this.locked ) {
        if ( e.shiftKey ) {
            if ( this.selected ) {
                mouser.unregisterShape(this);
            } else {
                mouser.registerShape(this);
                this.showHandles(true);
                this.selectHandles(true);
                this.registerHandles();
            }
        } else {
            if ( this.selected ) {
                this.selectHandles(true);
                this.registerHandles();
            } else {
                mouser.unregisterShapes();
                mouser.registerShape(this);
                this.showHandles(true);
                this.selectHandles(false);
            }
        }
    }
};

/*****
*
*   Circle.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Circle.prototype             = new Shape();
Circle.prototype.constructor = Circle;
Circle.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Circle(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
******/
Circle.prototype.init = function(svgNode) {
    if ( svgNode.localName == "circle" ) {
        // Call superclass method
        Circle.superclass.init.call(this, svgNode);

        // Init properties
        var cx = parseFloat( svgNode.getAttributeNS(null, "cx") );
        var cy = parseFloat( svgNode.getAttributeNS(null, "cy") );
        var r  = parseFloat( svgNode.getAttributeNS(null, "r")  );

        this.center = new Handle(cx, cy, this);
        this.last   = new Point2D(cx, cy);
        this.radius = new Handle(cx+r, cy, this);
    } else {
        throw new Error("Circle.init: Invalid SVG Node: " + svgNode.localName);
    }
};


/*****
*
*   realize
*
*****/
Circle.prototype.realize = function() {
    if ( this.svgNode != null ) {
        this.center.realize();
        this.radius.realize();

        this.center.show(false);
        this.radius.show(false);

        this.svgNode.addEventListener("mousedown", this, false);
    }
};


/*****
*
*   translate
*
*****/
Circle.prototype.translate = function(delta) {
    this.center.translate(delta);
    this.radius.translate(delta);
    this.refresh();
};


/*****
*
*   refresh
*
*****/
Circle.prototype.refresh = function() {
    var r = this.radius.point.distanceFrom(this.center.point);

    this.svgNode.setAttributeNS(null, "cx", this.center.point.x);
    this.svgNode.setAttributeNS(null, "cy", this.center.point.y);
    this.svgNode.setAttributeNS(null, "r", r);
};


/*****
*
*   registerHandles
*
*****/
Circle.prototype.registerHandles = function() {
    mouser.register(this.center);
    mouser.register(this.radius);
};


/*****
*
*   unregisterHandles
*
*****/
Circle.prototype.unregisterHandles = function() {
    mouser.unregister(this.center);
    mouser.unregister(this.radius);
};


/*****
*
*   selectHandles
*
*****/
Circle.prototype.selectHandles = function(select) {
    this.center.select(select);
    this.radius.select(select);
};


/*****
*
*   showHandles
*
*****/
Circle.prototype.showHandles = function(state) {
    this.center.show(state);
    this.radius.show(state);
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Circle.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Circle",
        [
            this.center.point,
            parseFloat( this.svgNode.getAttributeNS(null, "r") )
        ]
    );
};
/*****
*
*   Ellipse.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Ellipse.prototype             = new Shape();
Ellipse.prototype.constructor = Ellipse;
Ellipse.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Ellipse(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
Ellipse.prototype.init = function(svgNode) {
    if ( svgNode == null || svgNode.localName != "ellipse" )
        throw new Error("Ellipse.init: Invalid localName: " + svgNode.localName);
    
    // Call superclass method
    Ellipse.superclass.init.call(this, svgNode);

    // Init properties
    var cx = parseFloat( svgNode.getAttributeNS(null, "cx") );
    var cy = parseFloat( svgNode.getAttributeNS(null, "cy") );
    var rx = parseFloat( svgNode.getAttributeNS(null, "rx") );
    var ry = parseFloat( svgNode.getAttributeNS(null, "ry") );

    // Create handles
    this.center  = new Handle(cx, cy, this);
    this.radiusX = new Handle(cx+rx, cy, this);
    this.radiusY = new Handle(cx, cy+ry, this);
};


/*****
*
*   realize
*
*****/
Ellipse.prototype.realize = function() {
    this.center.realize();
    this.radiusX.realize();
    this.radiusY.realize();

    //this.radiusX.constrain = Handle.CONSTRAIN_X;
    //this.radiusY.constrain = Handle.CONSTRAIN_Y;

    this.center.show(false);
    this.radiusX.show(false);
    this.radiusY.show(false);

    this.svgNode.addEventListener("mousedown", this, false);
};


/*****
*
*   refresh
*
*****/
Ellipse.prototype.refresh = function() {
    var rx = Math.abs(this.center.point.x - this.radiusX.point.x);
    var ry = Math.abs(this.center.point.y - this.radiusY.point.y);

    this.svgNode.setAttributeNS(null, "cx", this.center.point.x);
    this.svgNode.setAttributeNS(null, "cy", this.center.point.y);
    this.svgNode.setAttributeNS(null, "rx", rx);
    this.svgNode.setAttributeNS(null, "ry", ry);
};


/*****
*
*   registerHandles
*
*****/
Ellipse.prototype.registerHandles = function() {
    mouser.register(this.center);
    mouser.register(this.radiusX);
    mouser.register(this.radiusY);
};


/*****
*
*   unregisterHandles
*
*****/
Ellipse.prototype.unregisterHandles = function() {
    mouser.unregister(this.center);
    mouser.unregister(this.radiusX);
    mouser.unregister(this.radiusY);
};


/*****
*
*   selectHandles
*
*****/
Ellipse.prototype.selectHandles = function(select) {
    this.center.select(select);
    this.radiusX.select(select);
    this.radiusY.select(select);
};


/*****
*
*   showHandles
*
*****/
Ellipse.prototype.showHandles = function(state) {
    this.center.show(state);
    this.radiusX.show(state);
    this.radiusY.show(state);
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Ellipse.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Ellipse",
        [
            this.center.point,
            parseFloat( this.svgNode.getAttributeNS(null, "rx") ),
            parseFloat( this.svgNode.getAttributeNS(null, "ry") )
        ]
    );
};
/*****
*
*   Handle.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Handle.prototype             = new Shape();
Handle.prototype.constructor = Handle;
Handle.superclass            = Shape.prototype;


/*****
*
*   Class properties
*
*****/
Handle.NO_CONSTRAINTS = 0;
Handle.CONSTRAIN_X    = 1;
Handle.CONSTRAIN_Y    = 2;


/*****
*
*   constructor
*
*****/
function Handle(x, y, owner) {
    if ( arguments.length > 0 ) {
        this.init(x, y, owner);
    }
}


/*****
*
*   init
*
*****/
Handle.prototype.init = function(x, y, owner) {
    // Call superclass method
    Handle.superclass.init.call(this, null);

    // Init properties
    this.point = new Point2D(x, y);
    this.owner = owner;

    this.constrain = Handle.NO_CONSTRAINTS;
    // build handle graphic
    //this.realize();
}


/*****
*
*   realize
*
*****/
Handle.prototype.realize = function() {
    if ( this.svgNode == null ) {
        var svgns = "http://www.w3.org/2000/svg";
        var handle = svgDocument.createElementNS(svgns, "rect");
        var parent;

        if ( this.owner != null && this.owner.svgNode != null ) {
            parent = this.owner.svgNode.parentNode;
        } else {
            parent = svgDocument.documentElement;
        }

        handle.setAttributeNS(null, "x", this.point.x - 2);
        handle.setAttributeNS(null, "y", this.point.y - 2);
        handle.setAttributeNS(null, "width", 4);
        handle.setAttributeNS(null, "height", 4);
        handle.setAttributeNS(null, "stroke", "black");
        handle.setAttributeNS(null, "fill", "white");
        handle.addEventListener("mousedown", this, false);

        parent.appendChild(handle);
        this.svgNode = handle;

        this.show( this.visible );
    }
};


/*****
*
*   translate
*
*****/
Handle.prototype.translate = function(delta) {
    if ( this.constrain == Handle.CONSTRAIN_X ) {
        this.point.x += delta.x;
    } else if ( this.constrain == Handle.CONSTRAIN_Y ) {
        this.point.y += delta.y;
    } else {
        this.point.addEquals(delta);
    }
    this.refresh();
};


/*****
*
*   refresh
*
*****/
Handle.prototype.refresh = function() {
    this.svgNode.setAttributeNS(null, "x", this.point.x - 2);
    this.svgNode.setAttributeNS(null, "y", this.point.y - 2);
};


/*****
*
*   select
*
*****/
Handle.prototype.select = function(state) {
    // call superclass method
    Handle.superclass.select.call(this, state);

    if ( state ) {
        this.svgNode.setAttributeNS(null, "fill", "black");
    } else {
        this.svgNode.setAttributeNS(null, "fill", "white");
    }
};


/*****
*
*   mousedown
*
*****/
Handle.prototype.mousedown = function(e) {
    if ( !this.locked ) {
        if ( e.shiftKey ) {
            if ( this.selected ) {
                mouser.unregister(this);
            } else {
                mouser.register(this);
                mouser.beginDrag(e);
            }
        } else {
            if ( !this.selected ) {
                var owner = this.owner;
                
                mouser.unregisterAll();
                mouser.register(this);
            }
            mouser.beginDrag(e);
        }
    }
};

/*****
*
*   Lever.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
Lever.prototype             = new Shape();
Lever.prototype.constructor = Lever;
Lever.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Lever(x1, y1, x2, y2, owner) {
    if ( arguments.length > 0 ) {
        this.init(x1, y1, x2, y2, owner);
    }
}


/*****
*
*   init
*
*****/
Lever.prototype.init = function(x1, y1, x2, y2, owner) {
    // call superclass method
    Lever.superclass.init.call(this, null);

    // init properties
    this.point = new Handle(x1, y1, this);
    this.lever = new LeverHandle(x2, y2, this);
    this.owner = owner;
};


/*****
*
*   realize
*
*****/
Lever.prototype.realize = function() {
    if ( this.svgNode == null ) {
        var svgns = "http://www.w3.org/2000/svg";
        var line = svgDocument.createElementNS(svgns, "line");
        var parent;

        if ( this.owner != null && this.owner.svgNode != null ) {
            parent = this.owner.svgNode.parentNode;
        } else {
            parent = svgDocument.documentElement;
        }

        line.setAttributeNS(null, "x1", this.point.point.x);
        line.setAttributeNS(null, "y1", this.point.point.y);
        line.setAttributeNS(null, "x2", this.lever.point.x);
        line.setAttributeNS(null, "y2", this.lever.point.y);
        line.setAttributeNS(null, "stroke", "black");

        parent.appendChild(line);
        this.svgNode = line;
        
        this.point.realize();
        this.lever.realize();

        this.show( this.visible );
    }
};


/*****
*
*   refresh
*
*****/
Lever.prototype.refresh = function() {
    this.svgNode.setAttributeNS(null, "x1", this.point.point.x);
    this.svgNode.setAttributeNS(null, "y1", this.point.point.y);
    this.svgNode.setAttributeNS(null, "x2", this.lever.point.x);
    this.svgNode.setAttributeNS(null, "y2", this.lever.point.y);
};

/*****
*
*   LeverHandle.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
LeverHandle.prototype             = new Handle();
LeverHandle.prototype.constructor = LeverHandle;
LeverHandle.superclass            = Handle.prototype;


/*****
*
*   constructor
*
*****/
function LeverHandle(x, y, owner) {
    if ( arguments.length > 0 ) {
        this.init(x, y, owner);
    }
}


/*****
*
*   realize
*
*****/
LeverHandle.prototype.realize = function() {
    if ( this.svgNode == null ) {
        var svgns = "http://www.w3.org/2000/svg";
        var handle = svgDocument.createElementNS(svgns, "circle");
        var parent;

        if ( this.owner != null && this.owner.svgNode != null ) {
            parent = this.owner.svgNode.parentNode;
        } else {
            parent = svgDocument.documentElement;
        }

        handle.setAttributeNS(null, "cx", this.point.x);
        handle.setAttributeNS(null, "cy", this.point.y);
        handle.setAttributeNS(null, "r", 2.5);
        handle.setAttributeNS(null, "fill", "black");
        handle.addEventListener("mousedown", this, false);

        parent.appendChild(handle);
        this.svgNode = handle;

        this.show( this.visible );
    }
};


/*****
*
*   refresh
*
*****/
LeverHandle.prototype.refresh = function() {
    this.svgNode.setAttributeNS(null, "cx", this.point.x);
    this.svgNode.setAttributeNS(null, "cy", this.point.y);
};


/*****
*
*   select
*
*****/
LeverHandle.prototype.select = function(state) {
    // call superclass method
    LeverHandle.superclass.select.call(this, state);

    this.svgNode.setAttributeNS(null, "fill", "black");
};

/*****
*
*   Line.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Line.prototype             = new Shape();
Line.prototype.constructor = Line;
Line.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Line(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
Line.prototype.init = function(svgNode) {
    if ( svgNode == null || svgNode.localName != "line" )
        throw new Error("Line.init: Invalid localName: " + svgNode.localName);
    
    // Call superclass method
    Line.superclass.init.call(this, svgNode);

    // Init properties
    var x1 = parseFloat( svgNode.getAttributeNS(null, "x1") );
    var y1 = parseFloat( svgNode.getAttributeNS(null, "y1") );
    var x2 = parseFloat( svgNode.getAttributeNS(null, "x2") );
    var y2 = parseFloat( svgNode.getAttributeNS(null, "y2") );

    // Create handles
    this.p1 = new Handle(x1, y1, this);
    this.p2 = new Handle(x2, y2, this);
};


/*****
*
*   realize
*
*****/
Line.prototype.realize = function() {
    this.p1.realize();
    this.p2.realize();

    this.p1.show(false);
    this.p2.show(false);

    this.svgNode.addEventListener("mousedown", this, false);
};


/*****
*
*   refresh
*
*****/
Line.prototype.refresh = function() {
    this.svgNode.setAttributeNS(null, "x1", this.p1.point.x);
    this.svgNode.setAttributeNS(null, "y1", this.p1.point.y);
    this.svgNode.setAttributeNS(null, "x2", this.p2.point.x);
    this.svgNode.setAttributeNS(null, "y2", this.p2.point.y);
};


/*****
*
*   registerHandles
*
*****/
Line.prototype.registerHandles = function() {
    mouser.register(this.p1);
    mouser.register(this.p2);
};


/*****
*
*   unregisterHandles
*
*****/
Line.prototype.unregisterHandles = function() {
    mouser.unregister(this.p1);
    mouser.unregister(this.p2);
};


/*****
*
*   selectHandles
*
*****/
Line.prototype.selectHandles = function(select) {
    this.p1.select(select);
    this.p2.select(select);
};


/*****
*
*   showHandles
*
*****/
Line.prototype.showHandles = function(state) {
    this.p1.show(state);
    this.p2.show(state);
};


/*****
*
*   cut
*
*****/
Line.prototype.cut = function(t) {
    var cutPoint = this.p1.point.lerp(this.p2.point, t);
    var newLine  = this.svgNode.cloneNode(true);
    
    this.p2.point.setFromPoint(cutPoint);
    this.p2.update();
    
    if ( this.svgNode.nextSibling != null )
        this.svgNode.parentNode.insertBefore(
            newLine,
            this.svgNode.nextSibling
        );
    else
        this.svgNode.parentNode.appendChild( newLine );

    var line = new Line(newLine);
    line.realize();
    line.p1.point.setFromPoint(cutPoint);
    line.p1.update();
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Line.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Line",
        [ this.p1.point, this.p2.point ]
    );
};
/*****
*
*   Token.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Token constructor
*
*****/
function Token(type, text) {
    if ( arguments.length > 0 ) {
        this.init(type, text);
    }
}


/*****
*
*   init
*
*****/
Token.prototype.init = function(type, text) {
    this.type = type;
    this.text = text;
};


/*****
*
*   typeis
*
*****/
Token.prototype.typeis = function(type) {
    return this.type == type;
}

/*****
*
*   Path.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Path.prototype             = new Shape();
Path.prototype.constructor = Path;
Path.superclass            = Shape.prototype;


/*****
*
*   Class constants
*
*****/
Path.COMMAND = 0;
Path.NUMBER  = 1;
Path.EOD     = 2;

Path.PARAMS = {
    A: [ "rx", "ry", "x-axis-rotation", "large-arc-flag", "sweep-flag", "x", "y" ],
    a: [ "rx", "ry", "x-axis-rotation", "large-arc-flag", "sweep-flag", "x", "y" ],
    C: [ "x1", "y1", "x2", "y2", "x", "y" ],
    c: [ "x1", "y1", "x2", "y2", "x", "y" ],
    H: [ "x" ],
    h: [ "x" ],
    L: [ "x", "y" ],
    l: [ "x", "y" ],
    M: [ "x", "y" ],
    m: [ "x", "y" ],
    Q: [ "x1", "y1", "x", "y" ],
    q: [ "x1", "y1", "x", "y" ],
    S: [ "x2", "y2", "x", "y" ],
    s: [ "x2", "y2", "x", "y" ],
    T: [ "x", "y" ],
    t: [ "x", "y" ],
    V: [ "y" ],
    v: [ "y" ],
    Z: [],
    z: []
};


/*****
*
*   constructor
*
*****/
function Path(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
Path.prototype.init = function(svgNode) {
    if ( svgNode == null || svgNode.localName != "path" )
        throw new Error("Path.init: Invalid localName: " + svgNode.localName);
    
    // Call superclass method
    Path.superclass.init.call(this, svgNode);
    
    // Convert path data to segments
    this.segments = null;
    this.parseData( svgNode.getAttributeNS(null, "d") );
};


/*****
*
*   realize
*
*****/
Path.prototype.realize = function() {
    for ( var i = 0; i < this.segments.length; i++ ) {
        this.segments[i].realize();
    }

    this.svgNode.addEventListener("mousedown", this, false);
};


/*****
*
*   refresh
*
*****/
Path.prototype.refresh = function() {
    var d = new Array();

    for ( var i = 0; i < this.segments.length; i++ ) {
        d.push( this.segments[i].toString() );
    }

    this.svgNode.setAttributeNS(null, "d", d.join(" "));
};


/*****
*
*   registerHandles
*
*****/
Path.prototype.registerHandles = function() {
    for ( var i = 0; i < this.segments.length; i++ ) {
        this.segments[i].registerHandles();
    }
};


/*****
*
*   unregisterHandles
*
*****/
Path.prototype.unregisterHandles = function() {
    for ( var i = 0; i < this.segments.length; i++ ) {
        this.segments[i].unregisterHandles();
    }
};


/*****
*
*   selectHandles
*
*****/
Path.prototype.selectHandles = function(select) {
    for ( var i = 0; i < this.segments.length; i++ ) {
        this.segments[i].selectHandles(select);
    }
};


/*****
*
*   showHandles
*
*****/
Path.prototype.showHandles = function(state) {
    for ( var i = 0; i < this.segments.length; i++ ) {
        this.segments[i].showHandles(state);
    }
};


/*****
*
*   appendPathSegment
*
*****/
Path.prototype.appendPathSegment = function(segment) {
    segment.previous = this.segments[this.segments.length-1];

    this.segments.push(segment);
};


/*****
*
*   parseData
*
*****/
Path.prototype.parseData = function(d) {
    // convert path data to token array
    var tokens = this.tokenize(d);

    // point to first token in array
    var index = 0;

    // get the current token
    var token = tokens[index];

    // set mode to signify new path
    var mode = "BOD";

    // init segment array
    // NOTE: should destroy previous segment handles here
    this.segments = new Array();

    // Process all tokens
    while ( !token.typeis(Path.EOD) ) {
        var param_length;
        var params = new Array();

        if ( mode == "BOD" ) {
            // Start of new path.  Must be a moveto command
            if ( token.text == "M" || token.text == "m" ) {
                // Advance past command token
                index++;

                // Get count of numbers that must follow this command
                param_length = Path.PARAMS[token.text].length;

                // Set new parsing mode
                mode = token.text;
            } else {
                // Oops.  New path didn't start with a moveto command
                throw new Error("Path data must begin with a moveto command");
            }
        } else {
            // Currently in a path definition
            if ( token.typeis(Path.NUMBER) ) {
                // Many commands allow you to keep repeating parameters
                // without specifying the command again.  This handles
                // that case.
                param_length = Path.PARAMS[mode].length;
            } else {
                // Advance past command token
                index++; 

                // Get count of numbers that must follow this command
                param_length = Path.PARAMS[token.text].length;

                // Set new parsing mode
                mode = token.text;
            }
        }
        
        // Make sure we have enough tokens left to satisfy the number
        // of parameters we need for the last command
        if ( (index + param_length) < tokens.length ) {
            // Get each parameter
            for (var i = index; i < index + param_length; i++) {
                var number = tokens[i];
                
                // Make sure each parameter is a number.
                if ( number.typeis(Path.NUMBER) )
                    params[params.length] = number.text;
                else
                    throw new Error("Parameter type is not a number: " + mode + "," + number.text);
            }
            
            // NOTE: Should create add an appendPathSegment (careful, that
            // effects RelativePathSegments
            var segment;
            var length   = this.segments.length;
            var previous = ( length == 0 ) ? null : this.segments[length-1];
            switch (mode) {
                case "A": segment = new AbsoluteArcPath(        params, this, previous ); break;
                case "C": segment = new AbsoluteCurveto3(       params, this, previous ); break;
                case "c": segment = new RelativeCurveto3(       params, this, previous ); break;
                case "H": segment = new AbsoluteHLineto(        params, this, previous ); break;
                case "L": segment = new AbsoluteLineto(         params, this, previous ); break;
                case "l": segment = new RelativeLineto(         params, this, previous ); break;
                case "M": segment = new AbsoluteMoveto(         params, this, previous ); break;
                case "m": segment = new RelativeMoveto(         params, this, previous ); break;
                case "Q": segment = new AbsoluteCurveto2(       params, this, previous ); break;
                case "q": segment = new RelativeCurveto2(       params, this, previous ); break;
                case "S": segment = new AbsoluteSmoothCurveto3( params, this, previous ); break;
                case "s": segment = new RelativeSmoothCurveto3( params, this, previous ); break;
                case "T": segment = new AbsoluteSmoothCurveto2( params, this, previous ); break;
                case "t": segment = new RelativeSmoothCurveto2( params, this, previous ); break;
                case "Z": segment = new RelativeClosePath(      params, this, previous ); break;
                case "z": segment = new RelativeClosePath(      params, this, previous ); break;
                default:
                    throw new Error("Unsupported segment type: " + mode);
            };
            this.segments.push(segment);

            // advance to the next unused token
            index += param_length;

            // get current token
            token = tokens[index];

            // Lineto's follow moveto when no command follows moveto params
            if ( mode == "M" ) mode = "L";
            if ( mode == "m" ) mode = "l";
        } else {
            throw new Error("Path data ended before all parameters were found");
        }
    }
}


/*****
*
*   tokenize
*
*   Need to add support for scientific notation
*
*****/
Path.prototype.tokenize = function(d) {
    var tokens = new Array();

    while ( d != "" ) {
        if ( d.match(/^([ \t\r\n,]+)/) )
        {
            d = d.substr(RegExp.$1.length);
        }
        else if ( d.match(/^([aAcChHlLmMqQsStTvVzZ])/) )
        {
            tokens[tokens.length] = new Token(Path.COMMAND, RegExp.$1);
            d = d.substr(RegExp.$1.length);
        }
        else if ( d.match(/^(([-+]?[0-9]+(\.[0-9]*)?|[-+]?\.[0-9]+)([eE][-+]?[0-9]+)?)/) )
        {
            tokens[tokens.length] = new Token(Path.NUMBER, parseFloat(RegExp.$1));
            d = d.substr(RegExp.$1.length);
        }
        else
        {
            throw new Error("Unrecognized segment command: " + d);
            //d = "";
        }
    }

    tokens[tokens.length] = new Token(Path.EOD, null);

    return tokens;
}


/*****
*
*   intersection methods
*
*****/

/*****
*
*   intersectShape
*
*****/
Path.prototype.intersectShape = function(shape) {
    var result = new Intersection("No Intersection");

    for ( var i = 0; i < this.segments.length; i++ ) {
        var inter = Intersection.intersectShapes(this.segments[i],shape);

        result.appendPoints(inter.points);
    }

    if ( result.points.length > 0 ) result.status = "Intersection";

    return result;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Path.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Path",
        []
    );
};

/*****
*
*   AbsolutePathSegment.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   constructor
*
*****/
function AbsolutePathSegment(command, params, owner, previous) {
    if ( arguments.length > 0 )
        this.init(command, params, owner, previous);
};


/*****
*
*   init
*
*****/
AbsolutePathSegment.prototype.init = function(command, params, owner, previous) {
    this.command  = command;
    this.owner    = owner;
    this.previous = previous;
    this.handles  = new Array();

    var index = 0;
    while ( index < params.length ) {
        var handle = new Handle(params[index], params[index+1], owner);

        this.handles.push( handle );
        index += 2;
    }
};


/*****
*
*   realize
*
*****/
AbsolutePathSegment.prototype.realize = function() {
    for ( var i = 0; i < this.handles.length; i++ ) {
        var handle = this.handles[i];

        handle.realize();
        handle.show(false);
    }
};


/*****
*
*   registerHandles
*
*****/
AbsolutePathSegment.prototype.registerHandles = function() {
    for ( var i = 0; i < this.handles.length; i++ ) {
        mouser.register( this.handles[i] );
    }
};


/*****
*
*   unregisterHandles
*
*****/
AbsolutePathSegment.prototype.unregisterHandles = function() {
    for ( var i = 0; i < this.handles.length; i++ ) {
        mouser.unregister( this.handles[i] );
    }
};


/*****
*
*   selectHandles
*
*****/
AbsolutePathSegment.prototype.selectHandles = function(select) {
    for ( var i = 0; i < this.handles.length; i++ ) {
        this.handles[i].select(select);
    }
};


/*****
*
*   showHandles
*
*****/
AbsolutePathSegment.prototype.showHandles = function(state) {
    for ( var i = 0; i < this.handles.length; i++ ) {
        this.handles[i].show(state);
    }
};


/*****
*
*   toString
*
*****/
AbsolutePathSegment.prototype.toString = function() {
    var points  = new Array();
    var command = "";
    
    if ( this.previous == null || this.previous.constructor != this.constuctor )
        command = this.command;

    for ( var i = 0; i < this.handles.length; i++ ) {
        points.push( this.handles[i].point.toString() );
    }

    return command + points.join(" ");
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getLastPoint
*
*****/
AbsolutePathSegment.prototype.getLastPoint = function() {
    return this.handles[this.handles.length - 1].point;
};


/*****
*
*   getIntersectionParams
*
*****/
AbsolutePathSegment.prototype.getIntersectionParams = function() {
    return null;
};
/*****
*
*   AbsoluteArcPath.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteArcPath.prototype             = new AbsolutePathSegment();
AbsoluteArcPath.prototype.constructor = AbsoluteArcPath;
AbsoluteArcPath.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteArcPath(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("A", params, owner, previous);
    }
}


/*****
*
*   init
*
*****/
AbsoluteArcPath.prototype.init = function(command, params, owner, previous) {
    var point = new Array();
    var y = params.pop();
    var x = params.pop();

    point.push( x, y );
    AbsoluteArcPath.superclass.init.call(this, command, point, owner, previous);

    this.rx        = parseFloat( params.shift() );
    this.ry        = parseFloat( params.shift() );
    this.angle     = parseFloat( params.shift() );
    this.arcFlag   = parseFloat( params.shift() );
    this.sweepFlag = parseFloat( params.shift() );
};


/*****
*
*   toString
*
*   override to handle case when Moveto is previous command
*
*****/
AbsoluteArcPath.prototype.toString = function() {
    var points  = new Array();
    var command = "";
    
    if ( this.previous.constructor != this.constuctor )
        command = this.command;

    return command +
        [
            this.rx, this.ry,
            this.angle, this.arcFlag, this.sweepFlag,
            this.handles[0].point.toString()
        ].join(",");
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
AbsoluteArcPath.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Ellipse",
        [
            this.getCenter(),
            this.rx, this.ry
        ]
    );
};


/*****
*
*   getCenter
*
*****/
AbsoluteArcPath.prototype.getCenter = function() {
    var startPoint = this.previous.getLastPoint();
    var endPoint   = this.handles[0].point;
    var rx         = this.rx;
    var ry         = this.ry;
    var angle      = this.angle * Math.PI / 180;
    var c          = Math.cos(angle);
    var s          = Math.sin(angle);
    var TOLERANCE  = 1e-6;

    var halfDiff = startPoint.subtract(endPoint).divide(2);
    var x1p = halfDiff.x *  c + halfDiff.y * s;
    var y1p = halfDiff.x * -s + halfDiff.y * c;

    var x1px1p = x1p*x1p;
    var y1py1p = y1p*y1p;
    var lambda = ( x1px1p / (rx*rx) ) + ( y1py1p / (ry*ry) );
    if ( lambda > 1 ) {
        var factor = Math.sqrt(lambda);

        rx *= factor;
        ry *= factor;
    }
    
    var rxrx = rx*rx;
    var ryry = ry*ry;
    var rxy1 = rxrx * y1py1p;
    var ryx1 = ryry * x1px1p;
    var factor = (rxrx*ryry - rxy1 - ryx1) / (rxy1 + ryx1);

    if ( Math.abs(factor) < TOLERANCE ) factor = 0;
    
    var sq = Math.sqrt(factor);

    if ( this.arcFlag == this.sweepFlag ) sq = -sq;
    var mid = startPoint.add(endPoint).divide(2);
    var cxp = sq *  rx*y1p / ry;
    var cyp = sq * -ry*x1p / rx;

    return new Point2D(
        cxp*c - cyp*s + mid.x,
        cxp*s + cyp*c + mid.y
    );
};
/*****
*
*   AbsoluteCurveto2.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteCurveto2.prototype             = new AbsolutePathSegment();
AbsoluteCurveto2.prototype.constructor = AbsoluteCurveto2;
AbsoluteCurveto2.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteCurveto2(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("Q", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getControlPoint
*
*****/
AbsoluteCurveto2.prototype.getControlPoint = function() {
    return this.handles[0].point;
};


/*****
*
*   getIntersectionParams
*
*****/
AbsoluteCurveto2.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier2",
        [
            this.previous.getLastPoint(),
            this.handles[0].point,
            this.handles[1].point
        ]
    );
};

/*****
*
*   AbsoluteCurveto3.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteCurveto3.prototype             = new AbsolutePathSegment();
AbsoluteCurveto3.prototype.constructor = AbsoluteCurveto3;
AbsoluteCurveto3.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteCurveto3(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("C", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getLastControlPoint
*
*****/
AbsoluteCurveto3.prototype.getLastControlPoint = function() {
    return this.handles[1].point;
};


/*****
*
*   getIntersectionParams
*
*****/
AbsoluteCurveto3.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier3",
        [
            this.previous.getLastPoint(),
            this.handles[0].point,
            this.handles[1].point,
            this.handles[2].point
        ]
    );
};

/*****
*
*   AbsoluteHLineto.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteHLineto.prototype             = new AbsolutePathSegment();
AbsoluteHLineto.prototype.constructor = AbsoluteHLineto;
AbsoluteHLineto.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteHLineto(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("H", params, owner, previous);
    }
}


/*****
*
*   init
*
*****/
AbsoluteHLineto.prototype.init = function(command, params, owner, previous) {
    var prevPoint = previous.getLastPoint();
    var point = new Array();

    point.push( params.pop(), prevPoint.y );
    AbsoluteHLineto.superclass.init.call(this, command, point, owner, previous);
};


/*****
*
*   toString
*
*****/
AbsoluteHLineto.prototype.toString = function() {
    var points  = new Array();
    var command = "";
    
    if ( this.previous.constructor != this.constuctor )
        command = this.command;

    return command + this.handles[0].point.x;
};

/*****
*
*   AbsoluteLineto.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteLineto.prototype             = new AbsolutePathSegment();
AbsoluteLineto.prototype.constructor = AbsoluteLineto;
AbsoluteLineto.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteLineto(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("L", params, owner, previous);
    }
}


/*****
*
*   toString
*
*   override to handle case when Moveto is previous command
*
*****/
AbsoluteLineto.prototype.toString = function() {
    var points  = new Array();
    var command = "";
    
    if ( this.previous.constructor != this.constuctor )
        if ( this.previous.constructor != AbsoluteMoveto )
            command = this.command;

    return command + this.handles[0].point.toString();
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
AbsoluteLineto.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Line",
        [
            this.previous.getLastPoint(),
            this.handles[0].point
        ]
    );
};

/*****
*
*   AbsoluteMoveto.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteMoveto.prototype             = new AbsolutePathSegment();
AbsoluteMoveto.prototype.constructor = AbsoluteMoveto;
AbsoluteMoveto.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteMoveto(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("M", params, owner, previous);
    }
}


/*****
*
*   toString
*
*   override toString since moveto's do not have shortcuts
*
*****/
AbsoluteMoveto.prototype.toString = function() {
    return "M" + this.handles[0].point.toString();
};

/*****
*
*   AbsoluteSmoothCurveto2.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteSmoothCurveto2.prototype             = new AbsolutePathSegment();
AbsoluteSmoothCurveto2.prototype.constructor = AbsoluteSmoothCurveto2;
AbsoluteSmoothCurveto2.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteSmoothCurveto2(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("T", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getControlPoint
*
*****/
AbsoluteSmoothCurveto2.prototype.getControlPoint = function() {
    var lastPoint = this.previous.getLastPoint();
    var point;
    
    if ( this.previous.command.match(/^[QqTt]$/) ) {
        var ctrlPoint = this.previous.getControlPoint();
        var diff      = ctrlPoint.subtract(lastPoint);

        point = lastPoint.subtract(diff);
    } else {
        point = lastPoint;
    }

    return point;
};


/*****
*
*   getIntersectionParams
*
*****/
AbsoluteSmoothCurveto2.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier2",
        [
            this.previous.getLastPoint(),
            this.getControlPoint(),
            this.handles[0].point
        ]
    );
};

/*****
*
*   AbsoluteSmoothCurveto3.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
AbsoluteSmoothCurveto3.prototype             = new AbsolutePathSegment();
AbsoluteSmoothCurveto3.prototype.constructor = AbsoluteSmoothCurveto3;
AbsoluteSmoothCurveto3.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function AbsoluteSmoothCurveto3(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("S", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getFirstControlPoint
*
*****/
AbsoluteSmoothCurveto3.prototype.getFirstControlPoint = function() {
    var lastPoint = this.previous.getLastPoint();
    var point;
    
    if ( this.previous.command.match(/^[SsCc]$/) ) {
        var lastControl = this.previous.getLastControlPoint();
        var diff        = lastControl.subtract(lastPoint);

        point = lastPoint.subtract(diff);
    } else {
        point = lastPoint;
    }

    return point;
};


/*****
*
*   getLastControlPoint
*
*****/
AbsoluteSmoothCurveto3.prototype.getLastControlPoint = function() {
    return this.handles[0].point;
};


/*****
*
*   getIntersectionParams
*
*****/
AbsoluteSmoothCurveto3.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier3",
        [
            this.previous.getLastPoint(),
            this.getFirstControlPoint(),
            this.handles[0].point,
            this.handles[1].point
        ]
    );
};

/*****
*
*   RelativePathSegment.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativePathSegment.prototype             = new AbsolutePathSegment();
RelativePathSegment.prototype.constructor = RelativePathSegment;
RelativePathSegment.superclass            = AbsolutePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativePathSegment(command, params, owner, previous) {
    if ( arguments.length > 0 )
        this.init(command, params, owner, previous);
}


/*****
*
*   init
*
*****/
RelativePathSegment.prototype.init = function(command, params, owner, previous) {
    this.command  = command;
    this.owner    = owner;
    this.previous = previous;
    this.handles  = new Array();

    var lastPoint;
    if ( this.previous )
        lastPoint = this.previous.getLastPoint();
    else
        lastPoint = new Point2D(0,0);

    var index = 0;
    while ( index < params.length ) {
        var handle = new Handle(
            lastPoint.x + params[index],
            lastPoint.y + params[index+1],
            owner
        );

        this.handles.push( handle );
        index += 2;
    }
};


/*****
*
*   toString
*
*****/
RelativePathSegment.prototype.toString = function() {
    var points    = new Array();
    var command   = "";
    var lastPoint;

    if ( this.previous )
        lastPoint = this.previous.getLastPoint();
    else
        lastPoint = new Point2D(0,0);

    if ( this.previous == null || this.previous.constructor != this.constructor )
        command = this.command;

    for ( var i = 0; i < this.handles.length; i++ ) {
        var point = this.handles[i].point.subtract( lastPoint );

        points.push( point.toString() );
    }
    
    return command + points.join(" ");
};
/*****
*
*   RelativeClosePath.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeClosePath.prototype             = new RelativePathSegment();
RelativeClosePath.prototype.constructor = RelativeClosePath;
RelativeClosePath.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeClosePath(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("z", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getLastPoint
*
*****/
RelativeClosePath.prototype.getLastPoint = function() {
    var current = this.previous;
    var point;

    while ( current ) {
        if ( current.command.match(/^[mMzZ]$/) ) {
            point = current.getLastPoint();
            break;
        }
        current = current.previous;
    }

    return point;
};


/*****
*
*   getIntersectionParams
*
*****/
RelativeClosePath.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Line",
        [
            this.previous.getLastPoint(),
            this.getLastPoint()
        ]
    );
};

/*****
*
*   RelativeCurveto2.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeCurveto2.prototype             = new RelativePathSegment();
RelativeCurveto2.prototype.constructor = RelativeCurveto2;
RelativeCurveto2.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeCurveto2(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("q", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getControlPoint
*
*****/
RelativeCurveto2.prototype.getControlPoint = function() {
    return this.handles[0].point;
};


/*****
*
*   getIntersectionParams
*
*****/
RelativeCurveto2.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier2",
        [
            this.previous.getLastPoint(),
            this.handles[0].point,
            this.handles[1].point
        ]
    );
};

/*****
*
*   RelativeCurveto3.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeCurveto3.prototype             = new RelativePathSegment();
RelativeCurveto3.prototype.constructor = RelativeCurveto3;
RelativeCurveto3.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeCurveto3(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("c", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getLastControlPoint
*
*****/
RelativeCurveto3.prototype.getLastControlPoint = function() {
    return this.handles[1].point;
};


/*****
*
*   getIntersectionParams
*
*****/
RelativeCurveto3.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier3",
        [
            this.previous.getLastPoint(),
            this.handles[0].point,
            this.handles[1].point,
            this.handles[2].point
        ]
    );
};

/*****
*
*   RelativeLineto.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeLineto.prototype             = new RelativePathSegment();
RelativeLineto.prototype.constructor = RelativeLineto;
RelativeLineto.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeLineto(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("l", params, owner, previous);
    }
}


/*****
*
*   toString
*
*   override to handle case when Moveto is previous command
*
*****/
RelativeLineto.prototype.toString = function() {
    var points  = new Array();
    var command = "";
    var lastPoint;
    var point;

    if ( this.previous )
        lastPoint = this.previous.getLastPoint();
    else
        lastPoint = new Point(0,0);

    point = this.handles[0].point.subtract( lastPoint );
    
    if ( this.previous.constructor != this.constuctor )
        if ( this.previous.constructor != RelativeMoveto )
            cmd = this.command;

    return cmd + point.toString();
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
RelativeLineto.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Line",
        [
            this.previous.getLastPoint(),
            this.handles[0].point
        ]
    );
};

/*****
*
*   RelativeMoveto.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeMoveto.prototype             = new RelativePathSegment();
RelativeMoveto.prototype.constructor = RelativeMoveto;
RelativeMoveto.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeMoveto(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("m", params, owner, previous);
    }
}


/*****
*
*   toString
*
*   override toString since moveto's do not have shortcuts
*
*****/
RelativeMoveto.prototype.toString = function() {
    return "m" + this.handles[0].point.toString();
};

/*****
*
*   RelativeSmoothCurveto2.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeSmoothCurveto2.prototype             = new RelativePathSegment();
RelativeSmoothCurveto2.prototype.constructor = RelativeSmoothCurveto2;
RelativeSmoothCurveto2.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeSmoothCurveto2(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("t", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getControlPoint
*
*****/
RelativeSmoothCurveto2.prototype.getControlPoint = function() {
    var lastPoint = this.previous.getLastPoint();
    var point;
    
    if ( this.previous.command.match(/^[QqTt]$/) ) {
        var ctrlPoint = this.previous.getControlPoint();
        var diff      = ctrlPoint.subtract(lastPoint);

        point = lastPoint.subtract(diff);
    } else {
        point = lastPoint;
    }

    return point;
};


/*****
*
*   getIntersectionParams
*
*****/
RelativeSmoothCurveto2.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier2",
        [
            this.previous.getLastPoint(),
            this.getControlPoint(),
            this.handles[0].point
        ]
    );
};

/*****
*
*   RelativeSmoothCurveto3.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
RelativeSmoothCurveto3.prototype             = new RelativePathSegment();
RelativeSmoothCurveto3.prototype.constructor = RelativeSmoothCurveto3;
RelativeSmoothCurveto3.superclass            = RelativePathSegment.prototype;


/*****
*
*   constructor
*
*****/
function RelativeSmoothCurveto3(params, owner, previous) {
    if ( arguments.length > 0 ) {
        this.init("s", params, owner, previous);
    }
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getFirstControlPoint
*
*****/
RelativeSmoothCurveto3.prototype.getFirstControlPoint = function() {
    var lastPoint = this.previous.getLastPoint();
    var point;
    
    if ( this.previous.command.match(/^[SsCc]$/) ) {
        var lastControl = this.previous.getLastControlPoint();
        var diff        = lastControl.subtract(lastPoint);

        point = lastPoint.subtract(diff);
    } else {
        point = lastPoint;
    }

    return point;
};


/*****
*
*   getLastControlPoint
*
*****/
RelativeSmoothCurveto3.prototype.getLastControlPoint = function() {
    return this.handles[0].point;
};


/*****
*
*   getIntersectionParams
*
*****/
RelativeSmoothCurveto3.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Bezier3",
        [
            this.previous.getLastPoint(),
            this.getFirstControlPoint(),
            this.handles[0].point,
            this.handles[1].point
        ]
    );
};

/*****
*
*   Polygon.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Polygon.prototype             = new Shape();
Polygon.prototype.constructor = Polygon;
Polygon.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Polygon(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
Polygon.prototype.init = function(svgNode) {
    if ( svgNode.localName == "polygon" ) {
        // Call superclass method
        Polygon.superclass.init.call(this, svgNode);

        // Init properties
        var points = svgNode.getAttributeNS(null, "points").split(/[\s,]+/);

        this.handles = new Array();
        for ( var i = 0; i < points.length; i += 2) {
            var x = parseFloat( points[i]   );
            var y = parseFloat( points[i+1] );
            
            this.handles.push( new Handle(x, y, this) );
        }
    } else {
        throw new Error("Polygon.init: Invalid SVG Node: " + svgNode.localName);
    }
};


/*****
*
*   realize
*
*****/
Polygon.prototype.realize = function() {
    if ( this.svgNode != null ) {
        for ( var i = 0; i < this.handles.length; i++ ) {
            this.handles[i].realize();
            this.handles[i].show(false);
        }

        this.svgNode.addEventListener("mousedown", this, false);
    }
};


/*****
*
*   refresh
*
*****/
Polygon.prototype.refresh = function() {
    var points = new Array();
    
    for ( var i = 0; i < this.handles.length; i++ ) {
        points.push( this.handles[i].point.toString() );
    }
    this.svgNode.setAttributeNS(null, "points", points.join(" "));
};


/*****
*
*   registerHandles
*
*****/
Polygon.prototype.registerHandles = function() {
    for ( var i = 0; i < this.handles.length; i++ )
        mouser.register( this.handles[i] );
};


/*****
*
*   unregisterHandles
*
*****/
Polygon.prototype.unregisterHandles = function() {
    for ( var i = 0; i < this.handles.length; i++ )
        mouser.unregister( this.handles[i] );
};


/*****
*
*   selectHandles
*
*****/
Polygon.prototype.selectHandles = function(select) {
    for ( var i = 0; i < this.handles.length; i++ )
        this.handles[i].select(select);
};


/*****
*
*   showHandles
*
*****/
Polygon.prototype.showHandles = function(state) {
    for ( var i = 0; i < this.handles.length; i++ )
        this.handles[i].show(state);
};


/*****
*
*   pointInPolygon
*
*****/
Polygon.prototype.pointInPolygon = function(point) {
    var length  = this.handles.length;
    var counter = 0;
    var x_inter;

    var p1 = this.handles[0].point;
    for ( var i = 1; i <= length; i++ ) {
        var p2 = this.handles[i%length].point;

        if ( point.y > Math.min(p1.y, p2.y)) {
            if ( point.y <= Math.max(p1.y, p2.y)) {
                if ( point.x <= Math.max(p1.x, p2.x)) {
                    if ( p1.y != p2.y ) {
                        x_inter = (point.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
                        if ( p1.x == p2.x || point.x <= x_inter) {
                            counter++;
                        }
                    }
                }
            }
        }
        p1 = p2;
    }

    return ( counter % 2 == 1 );
};


/*****
*
*   get/set/info methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Polygon.prototype.getIntersectionParams = function() {
    var points = new Array();
    for ( var i = 0; i < this.handles.length; i++ ) {
        points.push( this.handles[i].point );
    }
    
    return new IntersectionParams(
        "Polygon",
        [ points ]
    );
};


/*****
*
*   getArea
*
*****/
Polygon.prototype.getArea = function() {
    var area   = 0;
    var length = this.handles.length;
    var neg    = 0;
    var pos    = 0;

    for ( var i = 0; i < length; i++ ) {
        var h1 = this.handles[i].point;
        var h2 = this.handles[(i+1) % length].point;

        area += (h1.x * h2.y - h2.x * h1.y);
    }

    return area / 2;
};


/*****
*
*   getCentroid
*
*****/
Polygon.prototype.getCentroid = function() {
    var length = this.handles.length;
    var area6x = 6*this.getArea();
    var x_sum  = 0;
    var y_sum  = 0;

    for ( var i = 0; i < length; i++ ) {
        var p1    = this.handles[i].point;
        var p2    = this.handles[(i+1) % length].point;
        var cross = (p1.x*p2.y - p2.x*p1.y);

        x_sum += (p1.x + p2.x) * cross;
        y_sum += (p1.y + p2.y) * cross;
    }
    
    return new Point2D(x_sum / area6x, y_sum / area6x);
};


/*****
*
*   isClockwise
*
*****/
Polygon.prototype.isClockwise = function() {
    return this.getArea() < 0;
};


/*****
*
*   isCounterClockwise
*
*****/
Polygon.prototype.isCounterClockwise = function() {
    return this.getArea() > 0;
};


/*****
*
*   isConcave
*
*****/
Polygon.prototype.isConcave = function() {
    var positive = 0;
    var negative = 0;
    var length = this.handles.length;

    for ( var i = 0; i < length; i++) {
        var p0 = this.handles[i].point;
        var p1 = this.handles[(i+1) % length].point;
        var p2 = this.handles[(i+2) % length].point;
        var v0 = Vector2D.fromPoints(p0, p1);
        var v1 = Vector2D.fromPoints(p1, p2);
        var cross = v0.cross(v1);
        
        if ( cross < 0 ) {
            negative++;
        } else {
            positive++;
        }
    }

    return ( negative != 0 && positive != 0 );
};


/*****
*
*   isConvex
*
*****/
Polygon.prototype.isConvex = function() {
    return !this.isConcave();
};
/*****
*
*   Rectangle.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   Setup inheritance
*
*****/
Rectangle.prototype             = new Shape();
Rectangle.prototype.constructor = Rectangle;
Rectangle.superclass            = Shape.prototype;


/*****
*
*   constructor
*
*****/
function Rectangle(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
******/
Rectangle.prototype.init = function(svgNode) {
    if ( svgNode.localName == "rect" ) {
        // Call superclass method
        Rectangle.superclass.init.call(this, svgNode);

        // Init properties
        var x      = parseFloat( svgNode.getAttributeNS(null, "x") );
        var y      = parseFloat( svgNode.getAttributeNS(null, "y") );
        var width  = parseFloat( svgNode.getAttributeNS(null, "width")  );
        var height = parseFloat( svgNode.getAttributeNS(null, "height") );

        this.p1     = new Handle(x, y, this);               // top-left
        this.p2     = new Handle(x+width, y+height, this);  // bottom-right
    } else {
        throw new Error("Rectangle.init: Invalid SVG Node: " + svgNode.localName);
    }
};


/*****
*
*   realize
*
*****/
Rectangle.prototype.realize = function() {
    if ( this.svgNode != null ) {
        this.p1.realize();
        this.p2.realize();

        this.p1.show(false);
        this.p2.show(false);

        this.svgNode.addEventListener("mousedown", this, false);
    }
};


/*****
*
*   refresh
*
*****/
Rectangle.prototype.refresh = function() {
    var min = this.p1.point.min( this.p2.point );
    var max = this.p1.point.max( this.p2.point );

    this.svgNode.setAttributeNS(null, "x", min.x);
    this.svgNode.setAttributeNS(null, "y", min.y);
    this.svgNode.setAttributeNS(null, "width",  max.x - min.x);
    this.svgNode.setAttributeNS(null, "height", max.y - min.y);
};



/*****
*
*   registerHandles
*
*****/
Rectangle.prototype.registerHandles = function() {
    mouser.register(this.p1);
    mouser.register(this.p2);
};


/*****
*
*   unregisterHandles
*
*****/
Rectangle.prototype.unregisterHandles = function() {
    mouser.unregister(this.p1);
    mouser.unregister(this.p2);
};


/*****
*
*   selectHandles
*
*****/
Rectangle.prototype.selectHandles = function(select) {
    this.p1.select(select);
    this.p2.select(select);
};


/*****
*
*   showHandles
*
*****/
Rectangle.prototype.showHandles = function(state) {
    this.p1.show(state);
    this.p2.show(state);
};

/*****
*
*   get/set methods
*
*****/

/*****
*
*   getIntersectionParams
*
*****/
Rectangle.prototype.getIntersectionParams = function() {
    return new IntersectionParams(
        "Rectangle",
        [ this.p1.point, this.p2.point ]
    );
};
/*****
*
*   Table.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

var svgns = "http://www.w3.org/2000/svg";


/*****
*
*   setup inheritance
*
*****/
Table.prototype             = new EventHandler();
Table.prototype.constructor = Table;
Table.superclass            = EventHandler.prototype;


/*****
*
*   constructor
*
*****/
function Table(rowCount, columnCount) {
    if ( arguments.length > 0 )
        this.init(rowCount, columnCount);
}


/*****
*
*   init
*
*****/
Table.prototype.init = function(rowCount, columnCount) {
    this.owner       = null;
    this.x           = 0;
    this.y           = 0;
    this.rowCount    = rowCount;
    this.columnCount = columnCount;
    this.cells       = new Array();
    this._padding    = 6;
    this.svgNode     = null;
};


/*****
*
*   realize
*
*****/
Table.prototype.realize = function(svgParentNode) {
    var g = svgDocument.createElementNS(svgns, "g");
    
    for ( var i = 0; i < this.cells.length; i++ ) {
        var cell = this.cells[i];
        
        cell.realize(g);
        cell.owner = this;
    }
    
    svgParentNode.appendChild(g);
    this.svgNode = g;
};


/*****
*
*   createCell
*
*****/
Table.prototype.createCell = function() {
    return new TableCell();
};


/*****
*
*   setCell
*
*****/
Table.prototype.setCell = function(row, column, content) {
    var index = row * this.columnCount + column;

    this.cells[index].content = content;

    this.findRowHeight(row);
    this.findColumnWidth(column);
};


/*****
*
*   getCell
*
*****/
Table.prototype.getCell = function(row, column) {
    var index = row * this.columnCount + column;

    return this.cells[index];
};


/*****
*
*   update
*
*****/
Table.prototype.update = function() {
    var index = 0;
    var y     = 0;
    var widths = new Array();

    for ( var c = 0; c < this.columnCount; c++ )
        widths.push( this.findColumnWidth(c) );
    
    for ( var row = 0 ; row < this.rowCount; row++ ) {
        var x      = 0;
        var height = this.findRowHeight(row);
        
        for ( var column = 0; column < this.columnCount; column++ ) {
            var width = widths[column];
            var cell  = this.cells[index];
            
            if ( cell != null ) {
                cell.update(x, y, width, height);
                x += width;
            }

            index++;
        }
        y += height;
    }

    var bbox = this.svgNode.getBBox();
    var trans = "translate(" + this.x + "," + this.y + ")";

    this.svgNode.setAttributeNS(null, "transform", trans);
};


/*****
*
*   findRowHeight
*
*****/
Table.prototype.findRowHeight = function(row) {
    var max   = 0;
    var index = row * this.columnCount;

    for ( var i = 0; i < this.columnCount; i++ ) {
        max = Math.max(max, this.cells[index+i].height);
    }

    return max;
};


/*****
*
*   findColumnWidth
*
*****/
Table.prototype.findColumnWidth = function(column) {
    var max   = 0;
    var index = column;

    for ( var i = 0; i < this.rowCount; i++ ) {
        max = Math.max(max, this.cells[index].width);
        index += this.columnCount;
    }

    return max;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   get padding
*
*****/
Table.prototype.__defineGetter__(
    "padding",
    function() { return this._padding; }
);


/*****
*
*   set padding
*
*****/
Table.prototype.__defineSetter__(
    "padding",
    function(padding) {
        for ( var i = 0; i < this.cells.length; i++ ) {
            this.cells[i].padding = padding;
        }

        this._padding = padding;
    }
);


/*****
*
*   setPosition
*
*****/
Table.prototype.setPosition = function(x, y) {
    this.x = x;
    this.y = y;
};
/*****
*
*   TableCell.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
TableCell.prototype             = new EventHandler();
TableCell.prototype.constructor = TableCell;
TableCell.superclass            = EventHandler.prototype;


/*****
*
*   Class constants
*
*****/
TableCell.LEFT   = 0;
TableCell.MIDDLE = 1;
TableCell.RIGHT  = 2;
TableCell.TOP    = 3;
TableCell.BOTTOM = 4;


/*****
*
*   constructor
*
*****/
function TableCell() {
    this.init();
}


/*****
*
*   init
*
*****/
TableCell.prototype.init = function() {
    this.owner = null;

    this.hJustify = TableCell.LEFT;
    this.vJustify = TableCell.TOP;
    this.padding  = 6;

    this._width   = 10;
    this._height  = 10;
    this._content = null;

    this.svgNode    = null;
    this.background = null;
    this.foreground = null;
};


/*****
*
*   realize
*
*****/
TableCell.prototype.realize = function(svgParentNode) {
    var g = svgDocument.createElementNS(svgns, "g");
    var back = this.createBackground();
    var fore = svgDocument.createElementNS(svgns, "rect");

    g.appendChild(back);
    svgParentNode.appendChild(g);

    fore.setAttributeNS(null, "opacity", "0");

    this.svgNode = g;
    this.background = back;
    this.foreground = fore;

    if ( this._content != null )
        this.svgNode.appendChild(this._content);
};


/*****
*
*   createBackground
*
*****/
TableCell.prototype.createBackground = function() {
    var background = svgDocument.createElementNS(svgns, "rect");

    background.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");
    background.setAttributeNS(null, "stroke", "black");
    background.setAttributeNS(null, "stroke-width", "0.5");
    background.setAttributeNS(null, "opacity", "0.5");

    return background;
};


/*****
*
*   update
*
*****/
TableCell.prototype.update = function(x, y, width, height) {
    this.content.removeAttribute("transform");

    var bbox = this.content.getBBox();
    var tx, ty;
    
    switch ( this.hJustify ) {
        case TableCell.LEFT:   tx = this.padding; break;
        case TableCell.MIDDLE: tx = (width - bbox.width) / 2; break;
        case TableCell.RIGHT:  tx = width - bbox.width - this.padding; break;
        default:
            // raise error here?
            tx = 0;
    }

    switch ( this.vJustify ) {
        case TableCell.TOP:    ty = this.padding; break;
        case TableCell.MIDDLE: ty = (height - bbox.height) / 2; break;
        case TableCell.BOTTOM: ty = height - bbox.height - this.padding; break;
        default:
            // raise error here?
            ty = 0;
    }

    if ( this.background != null ) {
        this.background.setAttributeNS(null, "width",  width);
        this.background.setAttributeNS(null, "height", height);
    }

    this.foreground.setAttributeNS(null, "width",  width);
    this.foreground.setAttributeNS(null, "height", height);

    tx -= bbox.x;
    ty -= bbox.y;

    this.svgNode.setAttributeNS(null, "transform", "translate(" +x+ "," +y+ ")");
    this.content.setAttributeNS(null, "transform", "translate(" +tx+ "," +ty+ ")");
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   get content
*
*****/
TableCell.prototype.__defineGetter__(
    "content",
    function() {
        return this._content;
    }
);


/*****
*
*   get height
*
*****/
TableCell.prototype.__defineGetter__(
    "height",
    function() {
        var result = 2*this.padding;

        if ( this.svgNode != null ) {
            var bbox = this.svgNode.getBBox();

            result += bbox.height;
        }

        return Math.max(this._height, result);
    }
);


/*****
*
*   get width
*
*****/
TableCell.prototype.__defineGetter__(
    "width",
    function() {
        var result = 2 * this.padding;

        if ( this.svgNode != null ) {
            var bbox = this.svgNode.getBBox();

            result += bbox.width;
        }

        return Math.max(this._width, result);
    }
);


/*****
*
*   set content
*
*****/
TableCell.prototype.__defineSetter__(
    "content",
    function(content) {
        var bbox = content.getBBox();

        if ( bbox.x + bbox.y != 0 ) {
            var trans = "translate("+ (-bbox.x) +","+ (-bbox.y) + ")";

            content.setAttributeNS(null, "transform", trans);
        }
        if ( this.svgNode != null )
            this.svgNode.appendChild(content);

        this._content = content;
    }
);


/*****
*
*   set height
*
*****/
TableCell.prototype.__defineSetter__(
    "height",
    function(height) {
        this._height = height;
    }
);


/*****
*
*   set width
*
*****/
TableCell.prototype.__defineSetter__(
    "width",
    function(width) {
        this._width = width;
    }
);

/*****
*
*   MenuBar.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
MenuBar.prototype             = new Table();
MenuBar.prototype.constructor = MenuBar;
MenuBar.superclass            = Table.prototype;


/*****
*
*   constructor
*
*****/
function MenuBar() {
    this.init();
}


/*****
*
*   init
*
*****/
MenuBar.prototype.init = function() {
    // call superclass method
    MenuBar.superclass.init.call(this, 0, 0);

    // init properties
    this.current    = null;
    this.background = null;
};


/*****
*
*   realize
*
*****/
MenuBar.prototype.realize = function(svgParentNode) {
    // call superclass method
    MenuBar.superclass.realize.call(this, svgParentNode);

    var back = svgDocument.createElementNS(svgns, "rect");

    back.setAttributeNS(null, "width", "100%");
    back.setAttributeNS(null, "height", this.findRowHeight(0));
    back.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");
    back.addEventListener("mousedown", this, false);
    
    this.svgNode.insertBefore(back, this.svgNode.firstChild);
    
    for ( var i = 0; i < this.cells.length; i++ ) {
        this.cells[i].owner = this;
    }
};


/*****
*
*   appendMenu
*
*****/
MenuBar.prototype.appendMenu = function(menu) {
    this.cells.push(menu);
    this.columnCount++;
    
    if ( this.rowCount == 0 ) this.rowCount++;
};


/*****
*
*   select
*
*****/
MenuBar.prototype.select = function(menu) {
    if ( this.current != null ) this.current.select(false);
    if ( menu != null ) menu.select(true);
    this.current = menu;
};


/*****
*
*   mousedown
*
*****/
MenuBar.prototype.mousedown = function(e) {
    this.select(null);
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   getMenu
*
*****/
MenuBar.prototype.getMenu = function(index) {
    var menu;

    if ( 0 <= index && index < this.cells.length )
        menu = this.cells[index];

    return menu;
};

/*****
*
*   Menu.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
Menu.prototype             = new TableCell();
Menu.prototype.constructor = Menu;
Menu.superclass            = TableCell.prototype;


/*****
*
*   constructor
*
*****/
function Menu(title) {
    if ( arguments.length > 0 )
        this.init(title);
}


/*****
*
*   init
*
*****/
Menu.prototype.init = function(title) {
    // call superclass method
    Menu.superclass.init.call(this);

    // init properties
    this._title   = title;
    this.popup    = new Popup();
    this.selected = false;
};


/*****
*
*   realize
*
*****/
Menu.prototype.realize = function(svgParentNode) {
    // call superclass method
    Menu.superclass.realize.call(this, svgParentNode);
    
    // create content for menu
    var text  = svgDocument.createElementNS(svgns, "text");
    var tnode = svgDocument.createTextNode(this._title);

    text.setAttributeNS(null, "pointer-events", "none");
    text.appendChild(tnode);

    this.content = text;

    this.svgNode.addEventListener("mousedown", this, false);
    this.svgNode.addEventListener("mouseover", this, false);

    this.popup.realize(svgParentNode);
    this.popup.svgNode.setAttributeNS(null, "display", "none");
    this.popup.owner = this;
};


/*****
*
*   createBackground
*
*****/
Menu.prototype.createBackground = function() {
    var background = svgDocument.createElementNS(svgns, "rect");

    background.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");

    return background;
};


/*****
*
*   update
*
*****/
Menu.prototype.update = function(x, y, width, height) {
    // call superclass method
    Menu.superclass.update.call(this, x, y, width, height);

    this.popup.setPosition(x, height);
    this.popup.update();
};


/*****
*
*   appendItem
*
*****/
Menu.prototype.appendItem = function(item) {
    this.popup.appendItem(item);
};


/*****
*
*   event handlers
*
*****/

/*****
*
*   mousedown
*
*****/
Menu.prototype.mousedown = function(e) {
    if ( this.owner != null ) {
        if ( this.owner.current != null )
            if ( this.owner.current !== this )
                this.owner.select(this);
            else
                this.owner.select(null);
        else
            this.owner.select(this);
    }
};


/*****
*
*   mouseover
*
*****/
Menu.prototype.mouseover = function(e) {
    if ( this.owner != null )
        if ( this.owner.current != null )
            if ( this.owner.current !== this )
                this.owner.select(this);
};


/*****
*
*   select
*
*****/
Menu.prototype.select = function(select) {
    if ( select != this.selected ) {
        if ( select ) {
            this.background.setAttributeNS(null, "fill", "grey");
            this.popup.svgNode.setAttributeNS(null, "display", "inline");
        } else {
            this.background.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");
            this.popup.svgNode.setAttributeNS(null, "display", "none");
        }
        this.selected = select;
    }
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   set title
*
*****/
Menu.prototype.__defineSetter__(
    "title",
    function(title) {
        this._title = title;

        this.content.firstChild.data = title;
    }
);
/*****
*
*   Popup.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
Popup.prototype             = new Table();
Popup.prototype.constructor = Popup;
Popup.superclass            = Table.prototype;


/*****
*
*   constructor
*
*****/
function Popup() {
    this.init(0,0);
}


/*****
*
*   mousedown
*
*****/
Popup.prototype.mousedown = function(e) {
    if ( this.owner != null )
        this.owner.mousedown(e)
};


/*****
*
*   appendItem
*
*****/
Popup.prototype.appendItem = function(item) {
    this.cells.push(item);
    this.rowCount++;
    
    if ( this.columnCount == 0 ) this.columnCount++;
};

/*****
*
*   PopupItem.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
PopupItem.prototype             = new TableCell();
PopupItem.prototype.constructor = PopupItem;
PopupItem.superclass            = TableCell.prototype;


/*****
*
*   constructor
*
*****/
function PopupItem(name) {
    if ( arguments.length > 0 )
        this.init(name);
}


/*****
*
*   init
*
*****/
PopupItem.prototype.init = function(name) {
    // call superclass method
    PopupItem.superclass.init.call(this);

    // init properties
    this._name    = name;
    this.callback = null;
};


/*****
*
*   realize
*
*****/
PopupItem.prototype.realize = function(svgParentNode) {
    // call superclass method
    PopupItem.superclass.realize.call(this, svgParentNode);
    
    // create content for item
    var text  = svgDocument.createElementNS(svgns, "text");
    var tnode = svgDocument.createTextNode(this._name);

    text.setAttributeNS(null, "pointer-events", "none");
    text.appendChild(tnode);

    this.content = text;

    this.svgNode.addEventListener("mouseover", this, false);
    this.svgNode.addEventListener("mouseout",  this, false);
    this.svgNode.addEventListener("mousedown", this, false);
};


/*****
*
*   createBackground
*
*****/
PopupItem.prototype.createBackground = function() {
    var background = svgDocument.createElementNS(svgns, "rect");

    background.setAttributeNS(null, "fill", "rgb(75%,75%,75%)");

    return background;
};



/*****
*
*   event handlers
*
*****/

/*****
*
*   mouseover
*
*****/
PopupItem.prototype.mouseover = function(e) {
    this.background.setAttributeNS(null, "fill", "rgb(128,128,192)");
};


/*****
*
*   mouseout
*
*****/
PopupItem.prototype.mouseout = function(e) {
    this.background.setAttributeNS(null, "fill", "rgb(75%,75%,75%)");
};


/*****
*
*   mousedown
*
*****/
PopupItem.prototype.mousedown = function(e) {
    this.background.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");
    if ( this.owner != null )
        this.owner.mousedown(e);

    if ( this.callback != null )
        this.callback(this);
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   get name
*
*****/
PopupItem.prototype.__defineGetter__(
    "name",
    function() { return this._name; }
);


/*****
*
*   set name
*
*****/
PopupItem.prototype.__defineSetter__(
    "name",
    function(name) {
        this._name = name;

        this.content.firstChild.data = name;
    }
);
/*****
*
*   Window.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

/*****
*
*   setup inheritance
*
*****/
Window.prototype             = new EventHandler();
Window.prototype.constructor = Window;
Window.superclass            = EventHandler.prototype;


/*****
*
*   class variables
*
*****/
Window.SERIAL = 0;


/*****
*
*   constructor
*
*****/
function Window(x, y, width, height) {
    if ( arguments.length > 0 )
        this.init(x, y, width, height);
}


/*****
*
*   init
*
*****/
Window.prototype.init = function(x, y, width, height) {
    this.x        = ( x != null ) ? x : 0;
    this.y        = ( y != null ) ? y : 0;
    this.width    = ( width  != null ) ? width  : 100;
    this.height   = ( height != null ) ? height : 100;
    this.svgNode  = null;
    this.tbar     = null;
    this.last     = null;
    this._content = null;
};


/*****
*
*   realize
*
*****/
Window.prototype.realize = function(svgParentNode) {
    var gradient = svgDocument.getElementById("__window_title_bar__");
    var t = Window.SERIAL++;

    if ( gradient == null ) {
        gradient = svgDocument.createElementNS(svgns, "linearGradient");
        var stop1 = svgDocument.createElementNS(svgns, "stop");
        var stop2 = svgDocument.createElementNS(svgns, "stop");

        stop1.setAttributeNS(null, "offset", "0");
        stop1.setAttributeNS(null, "stop-color", "rgb(0,0,128)");
        stop2.setAttributeNS(null, "offset", "1");
        stop2.setAttributeNS(null, "stop-color", "rgb(128,128,192)");

        gradient.appendChild(stop1);
        gradient.appendChild(stop2);

        gradient.setAttributeNS(null, "id", "__window_title_bar__");

        svgDocument.documentElement.appendChild(gradient);
    }

    var g    = svgDocument.createElementNS(svgns, "g");
    var clip = svgDocument.createElementNS(svgns, "clipPath");
    var use  = svgDocument.createElementNS(svgns, "use");
    var back = svgDocument.createElementNS(svgns, "rect");
    var tbar = svgDocument.createElementNS(svgns, "rect");

    clip.setAttributeNS(null, "id", "__window_clipping_path__" + t);
    use.setAttributeNS(
        "http://www.w3.org/1999/xlink",
        "xlink:href",
        "#__window_background__" + t
    );
    clip.appendChild(use);
    svgParentNode.appendChild(clip);
    
    back.setAttributeNS(null, "width", this.width);
    back.setAttributeNS(null, "height", this.height);
    back.setAttributeNS(null, "stroke", "black");
    back.setAttributeNS(null, "stroke-width", "1");
    back.setAttributeNS(null, "fill", "rgb(90%,90%,90%)");
    back.setAttributeNS(null, "id", "__window_background__" + t);

    tbar.setAttributeNS(null, "width", this.width);
    tbar.setAttributeNS(null, "height", 16);
    tbar.setAttributeNS(null, "fill", "url(#__window_title_bar__)");
    tbar.addEventListener("mousedown", this, false);

    var trans = "translate(" + this.x + "," + this.y + ")";
    g.setAttributeNS(null, "transform", trans);

    g.setAttributeNS(null, "clip-path", "url(#__window_clipping_path__" + t + ")");
    g.appendChild(back);
    g.appendChild(tbar);

    svgDocument.documentElement.appendChild(g);

    this.svgNode = g;
    this.tbar    = tbar;
};


/*****
*
*   event handlers
*
*****/

/*****
*
*   mousedown
*
*****/
Window.prototype.mousedown = function(e) {
    this.last = { x: e.clientX, y:e.clientY };

    this.svgNode.parentNode.appendChild( this.svgNode );

    this.tbar.addEventListener("mousemove", this, false);
    this.tbar.addEventListener("mouseup", this, false);
};


/*****
*
*   mousemove
*
*****/
Window.prototype.mousemove = function(e) {
    this.x += e.clientX - this.last.x;
    this.y += e.clientY - this.last.y;

    var trans = "translate(" + this.x + "," + this.y + ")";
    this.svgNode.setAttributeNS(null, "transform", trans);

    this.last.x = e.clientX;
    this.last.y = e.clientY;
};


/*****
*
*   mouseup
*
*****/
Window.prototype.mouseup = function(e) {
    this.tbar.removeEventListener("mousemove", this, false);
    this.tbar.removeEventListener("mouseup", this, false);
    this.last = null;
};


/*****
*
*   get/set methods
*
*****/

/*****
*
*   set content
*
*****/
Window.prototype.__defineSetter__(
    "content",
    function(content) {
        var trans = "translate(0,16)";
        
        content.svgNode.setAttributeNS(null, "transform", trans);
        this.svgNode.appendChild(content.svgNode);
        this._content = content;
    }
);
