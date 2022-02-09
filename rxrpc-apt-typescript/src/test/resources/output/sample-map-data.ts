import { SampleData } from './sample-data';
import { SampleEnum } from './sample-enum';
import { StringKeyMap } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapData
 */
export class SampleMapData {
    sampleMap: StringKeyMap<SampleData>;
    sampleImmutableMap: StringKeyMap<SampleData>;
    sampleEnumMap: Map<SampleEnum, SampleEnum[]>;
}
