/*
  Leaflet.TileLayer.ColorFilter
  (c) 2018, Claudio T. Kawakani
  A simple and lightweight Leaflet plugin to apply CSS filters on map tiles.
  https://github.com/xtk93x/Leaflet.TileLayer.ColorFilter
*/
"use strict";L.TileLayer.ColorFilter=L.TileLayer.extend({intialize:function(t,i){L.TileLayer.prototype.initialize.call(this,t,i)},colorFilter:function(){var r=["blur:px","brightness:%","bright:brightness:%","bri:brightness:%","contrast:%","con:contrast:%","grayscale:%","gray:grayscale:%","hue-rotate:deg","hue:hue-rotate:deg","hue-rotation:hue-rotate:deg","invert:%","inv:invert:%","opacity:%","op:opacity:%","saturate:%","saturation:saturate:%","sat:saturate:%","sepia:%","sep:sepia:%"];return(this.options.filter?this.options.filter:[]).map(function(t){var i=t.toLowerCase().split(":");if(2===i.length){var e=r.find(function(t){return t.split(":")[0]===i[0]});if(e)return e=e.split(":"),i[1]+=/^\d+$/.test(i[1])?e[e.length-1]:"","".concat(e[e.length-2],"(").concat(i[1],")")}return""}).join(" ")},_initContainer:function(){L.TileLayer.prototype._initContainer.call(this);this._container.style.filter=this.colorFilter()},updateFilter:function(t){this.options.filter=t,this._container&&(this._container.style.filter=this.colorFilter())}}),L.tileLayer.colorFilter=function(t,i){return new L.TileLayer.ColorFilter(t,i)};
