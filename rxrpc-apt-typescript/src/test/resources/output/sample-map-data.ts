import { SampleData } from './index';
import { StringKeyMap } from 'rxrpc-types';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleMapData
 */
export interface SampleMapData {
    sampleMap: StringKeyMap<SampleData>;
    sampleImmutableMap: StringKeyMap<SampleData>;
}
