import { SampleData } from './index';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleRequest
 */
export interface SampleRequest {
    id: number;
    name: string;
    data: SampleData;
    mapData: Map<string, any>;
    multipleData: SampleData[];
}
