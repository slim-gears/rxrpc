import { SampleData } from './index';
import { StringKeyMap } from 'rxrpc';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapData
 */
export interface SampleMapData {
    sampleMap: StringKeyMap<SampleData>;
    sampleImmutableMap: StringKeyMap<SampleData>;
}
