// proj4leaflet 类型声明

import * as L from 'leaflet';

declare module 'leaflet' {
  namespace Proj {
    class CRS extends L.CRS {
      constructor(
        srsCode: string,
        proj4def: string,
        options?: {
          origin?: [number, number];
          resolutions?: number[];
          bounds?: L.BoundsExpression;
          transformation?: L.Transformation;
        }
      );
    }
  }
}

export {};
